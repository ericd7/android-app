package com.example.androidgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    
    private val gameThread: GameThread
    private val paint: Paint = Paint()
    
    // Game state
    private var gameRunning = false
    private var score = 0
    
    init {
        // Add the callback
        holder.addCallback(this)
        
        // Create the game thread
        gameThread = GameThread(holder, this)
        
        // Make the view focusable to handle events
        isFocusable = true
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        // Start the game thread
        gameThread.setRunning(true)
        gameThread.start()
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handle surface changes if needed
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Stop the game thread
        var retry = true
        gameThread.setRunning(false)
        while (retry) {
            try {
                gameThread.join()
                retry = false
            } catch (e: InterruptedException) {
                // Try again
            }
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Handle touch events
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    /**
     * Update game state
     */
    fun update() {
        if (!gameRunning) return
        
        // TODO: Update game objects and state
    }
    
    /**
     * Draw the game
     */

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        
        // Clear the screen
        canvas.drawColor(Color.BLACK)
        
        // Draw game elements
        paint.color = Color.WHITE
        paint.textSize = 50f
        canvas.drawText("Score: $score", 50f, 50f, paint)
        
        // TODO: Draw game objects
    }
    
    /**
     * Start the game
     */
    fun startGame() {
        gameRunning = true
        score = 0
    }
    
    /**
     * End the game
     */
    fun endGame() {
        gameRunning = false
        // TODO: Handle game over
    }
} 