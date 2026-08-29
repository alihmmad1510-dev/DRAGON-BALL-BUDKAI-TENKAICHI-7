package com.stickmanfighter.game.core

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.stickmanfighter.game.characters.StickmanFighter

/**
 * Phase 1 scope only: one controllable stickman, one idle stickman, a ground line,
 * and touch-driven movement/jump. Combat, AI, camera, and arenas arrive in later phases.
 */
class GameView(context: Context, attrs: AttributeSet? = null) :
    SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var gameLoop: GameLoop? = null

    private lateinit var player: StickmanFighter
    private lateinit var dummy: StickmanFighter

    private var groundY: Float = 0f

    private val groundPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 6f
    }
    private val backgroundColor = Color.rgb(210, 230, 245)

    // --- Touch control zones (recomputed on surfaceChanged for current screen size) ---
    private var leftButton = ButtonZone()
    private var rightButton = ButtonZone()
    private var jumpButton = ButtonZone()
    private val buttonPaint = Paint().apply {
        color = Color.argb(90, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val buttonLabelPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private var movingLeft = false
    private var movingRight = false

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameLoop = GameLoop(this).also {
            it.running = true
            it.start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        groundY = height * 0.8f

        player = StickmanFighter(x = width * 0.3f, y = groundY, facingRight = true, bodyColor = Color.BLACK)
        dummy = StickmanFighter(x = width * 0.7f, y = groundY, facingRight = false, bodyColor = Color.rgb(120, 20, 20))

        val buttonSize = height * 0.14f
        val margin = 30f
        leftButton = ButtonZone(margin, height - buttonSize - margin, margin + buttonSize, height - margin)
        rightButton = ButtonZone(
            margin * 2 + buttonSize, height - buttonSize - margin,
            margin * 2 + buttonSize * 2, height - margin
        )
        jumpButton = ButtonZone(
            width - buttonSize - margin, height - buttonSize - margin,
            width - margin, height - margin
        )
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        gameLoop?.running = false
        var retry = true
        while (retry) {
            try {
                gameLoop?.join()
                retry = false
            } catch (e: InterruptedException) {
                // keep retrying the join
            }
        }
    }

    fun update(deltaSeconds: Float) {
        if (!this::player.isInitialized) return

        player.velocityX = when {
            movingLeft && !movingRight -> -PLAYER_SPEED
            movingRight && !movingLeft -> PLAYER_SPEED
            else -> 0f
        }

        player.update(deltaSeconds, groundY)
        dummy.update(deltaSeconds, groundY)
    }

    fun renderFrame() {
        if (!holder.surface.isValid) return
        val canvas: Canvas = holder.lockCanvas() ?: return
        try {
            drawScene(canvas)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawScene(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
        canvas.drawLine(0f, groundY, width.toFloat(), groundY, groundPaint)

        if (this::player.isInitialized) {
            player.draw(canvas)
            dummy.draw(canvas)
        }

        drawButton(canvas, leftButton, "◀")
        drawButton(canvas, rightButton, "▶")
        drawButton(canvas, jumpButton, "JUMP")
    }

    private fun drawButton(canvas: Canvas, zone: ButtonZone, label: String) {
        canvas.drawRoundRect(zone.left, zone.top, zone.right, zone.bottom, 16f, 16f, buttonPaint)
        canvas.drawText(label, zone.centerX(), zone.centerY() + 12f, buttonLabelPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        movingLeft = false
        movingRight = false
        var jumpPressed = false

        val isReleaseEvent = event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_CANCEL

        for (i in 0 until event.pointerCount) {
            if (isReleaseEvent && event.pointerCount == 1) continue
            val px = event.getX(i)
            val py = event.getY(i)

            if (leftButton.contains(px, py)) movingLeft = true
            if (rightButton.contains(px, py)) movingRight = true
            if (jumpButton.contains(px, py)) jumpPressed = true
        }

        if (jumpPressed && this::player.isInitialized && player.onGround) {
            player.velocityY = -JUMP_VELOCITY
            player.onGround = false
        }

        return true
    }

    private data class ButtonZone(
        val left: Float = 0f,
        val top: Float = 0f,
        val right: Float = 0f,
        val bottom: Float = 0f
    ) {
        fun contains(px: Float, py: Float) = px in left..right && py in top..bottom
        fun centerX() = (left + right) / 2f
        fun centerY() = (top + bottom) / 2f
    }

    companion object {
        private const val PLAYER_SPEED = 320f
        private const val JUMP_VELOCITY = 780f
    }
}
