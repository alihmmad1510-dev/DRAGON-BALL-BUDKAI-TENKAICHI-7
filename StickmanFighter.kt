package com.stickmanfighter.game.characters

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Phase 1: a minimal, original stickman body drawn with primitives (no external assets).
 * Later phases (Character Data System, Combat, Transformations) will build on top of this.
 */
class StickmanFighter(
    var x: Float,
    var y: Float,
    val facingRight: Boolean,
    val bodyColor: Int = Color.BLACK
) {
    // Physics
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var onGround: Boolean = true

    // Dimensions (used later for hitboxes)
    val width: Float = 60f
    val height: Float = 160f

    // Simple walk-cycle timer, drives leg swing while moving
    private var walkCycle: Float = 0f

    private val paint = Paint().apply {
        color = bodyColor
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val headPaint = Paint().apply {
        color = bodyColor
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    fun update(deltaSeconds: Float, groundY: Float) {
        // Gravity
        if (!onGround) {
            velocityY += GRAVITY * deltaSeconds
        }

        x += velocityX * deltaSeconds
        y += velocityY * deltaSeconds

        // Ground collision
        if (y >= groundY) {
            y = groundY
            velocityY = 0f
            onGround = true
        }

        // Walk animation only advances while grounded and moving horizontally
        if (onGround && velocityX != 0f) {
            walkCycle += deltaSeconds * 10f
        } else {
            walkCycle = 0f
        }
    }

    fun draw(canvas: Canvas) {
        val headRadius = 18f
        val headCenterY = y - height
        val headCenterX = x

        // Head
        canvas.drawCircle(headCenterX, headCenterY + headRadius, headRadius, headPaint)

        // Spine (neck to hip)
        val neckY = headCenterY + headRadius * 2
        val hipY = y - height * 0.4f
        canvas.drawLine(x, neckY, x, hipY, paint)

        // Legs (swing based on walkCycle)
        val legSwing = Math.sin(walkCycle.toDouble()).toFloat() * 20f
        val footY = y
        canvas.drawLine(x, hipY, x - 20f + legSwing, footY, paint)
        canvas.drawLine(x, hipY, x + 20f - legSwing, footY, paint)

        // Arms (opposite swing from legs for a natural stride)
        val shoulderY = neckY + 10f
        val armSwing = -legSwing
        canvas.drawLine(x, shoulderY, x - 25f + armSwing, shoulderY + 40f, paint)
        canvas.drawLine(x, shoulderY, x + 25f - armSwing, shoulderY + 40f, paint)
    }

    companion object {
        const val GRAVITY = 1800f // px/sec^2, tuned in later phases against real device DPI
    }
}
