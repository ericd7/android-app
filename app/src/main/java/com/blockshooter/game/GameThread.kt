package com.blockshooter.game

import android.graphics.Canvas
import android.os.Process
import android.view.SurfaceHolder

class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {
    
    private var running: Boolean = false
    private val targetFPS = 60
    private val targetTime = (1000 / targetFPS).toLong()
    
    // Frame stats for monitoring performance
    private var frameCount = 0
    private var lastFpsTime = 0L
    private var currentFPS = 0
    
    // Maximum allowed delta time to prevent physics issues after lag spikes
    private val maxDeltaTime = 0.05f // 50ms
    
    fun setRunning(isRunning: Boolean) {
        this.running = isRunning
    }
    
    override fun run() {
        // Set thread priority to improve performance
        Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY)
        
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long
        var lastFrameTime = System.nanoTime()
        lastFpsTime = System.currentTimeMillis()
        
        while (running) {
            startTime = System.nanoTime()
            var canvas: Canvas? = null
            
            try {
                // Get Canvas from Holder and lock it
                canvas = surfaceHolder.lockCanvas()
                
                if (canvas != null) {
                    // Calculate delta time in seconds
                    val currentTime = System.nanoTime()
                    var deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f // Convert nanoseconds to seconds
                    lastFrameTime = currentTime
                    
                    // Cap delta time to prevent physics issues after lag spikes
                    if (deltaTime > maxDeltaTime) {
                        deltaTime = maxDeltaTime
                    }
                    
                    // Synchronized block to avoid data inconsistencies
                    synchronized(surfaceHolder) {
                        // Update game state
                        gameView.update(deltaTime)
                        
                        // Render state to the canvas
                        gameView.draw(canvas)
                    }
                    
                    // Update FPS counter
                    frameCount++
                    val now = System.currentTimeMillis()
                    if (now - lastFpsTime > 1000) {
                        currentFPS = frameCount
                        frameCount = 0
                        lastFpsTime = now
                    }
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
            } else {
                // If we're running behind, yield to let other threads run
                yield()
            }
        }
    }
    
    /**
     * Gets the current FPS.
     * 
     * @return The current FPS
     */
    fun getCurrentFPS(): Int {
        return currentFPS
    }
} 