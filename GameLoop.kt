package com.stickmanfighter.game.core

/**
 * Drives update()/draw() on GameView at a target FPS.
 * Phase 23 (Android Optimization) will extend this with render-scale and FPS caps
 * pulled from Settings; kept simple and isolated here so that later work does not
 * require touching GameView's rendering code.
 */
class GameLoop(
    private val target: GameView,
    private val targetFps: Int = 60
) : Thread("StickmanFighter-GameLoop") {

    @Volatile
    var running: Boolean = false

    override fun run() {
        val targetFrameNanos = 1_000_000_000L / targetFps
        var lastTime = System.nanoTime()

        while (running) {
            val now = System.nanoTime()
            val deltaSeconds = ((now - lastTime).coerceAtMost(targetFrameNanos * 5)) / 1_000_000_000f
            lastTime = now

            target.update(deltaSeconds)
            target.renderFrame()

            val frameNanos = System.nanoTime() - now
            val sleepNanos = targetFrameNanos - frameNanos
            if (sleepNanos > 0) {
                try {
                    sleep(sleepNanos / 1_000_000L, (sleepNanos % 1_000_000L).toInt())
                } catch (e: InterruptedException) {
                    running = false
                }
            }
        }
    }
}
