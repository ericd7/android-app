package com.example.androidgame

import android.graphics.Canvas
import android.view.SurfaceHolder

class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {
    
    private var running: Boolean = false
    private val targetFPS = 60
    private val targetTime = (1000 / targetFPS).toLong()
    
    fun setRunning(isRunning: Boolean) {
        this.running = isRunning
    }
    
    override fun run() {
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long
        
        while (running) {
            startTime = System.nanoTime()
            var canvas: Canvas? = null
            
            try {
                // Get Canvas from Holder and lock it
                canvas = surfaceHolder.lockCanvas()
                
                // Synchronized block to avoid data inconsistencies
                synchronized(surfaceHolder) {
                    // Update game state
                    gameView.update()
                    
                    // Render state to the canvas
                    canvas?.let { gameView.draw(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // In case of an exception the surface is not left in an inconsistent state
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            // Calculate how long the frame took
            timeMillis = (System.nanoTime() - startTime) / 1000000
            
            // Calculate how long to wait to maintain target FPS
            waitTime = targetTime - timeMillis
            
            // Sleep to maintain consistent frame rate
            if (waitTime > 0) {
                try {
                    sleep(waitTime)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
} 