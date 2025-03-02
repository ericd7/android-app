package com.blockshooter.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import com.blockshooter.game.effects.GlowEffect
import com.blockshooter.game.effects.ParticleSystem
import com.blockshooter.game.effects.ScreenShakeEffect
import com.blockshooter.game.effects.SoundManager
import com.blockshooter.game.util.GameManager
import com.blockshooter.game.util.GameRenderer

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    
    private val gameThread: GameThread
    private val paint: Paint = Paint()
    
    // Sound effects
    private var toneGenerator: ToneGenerator? = null
    private val soundEnabled: Boolean
    
    // Screen dimensions
    private var screenWidth = 0
    private var screenHeight = 0
    
    // Game over UI elements
    private lateinit var restartButton: RectF
    private lateinit var backButton: RectF
    
    // Game over listener
    private var gameOverListener: ((Int) -> Unit)? = null
    
    // Game colors
    private val backgroundColor = ContextCompat.getColor(context, R.color.game_background)
    private val primaryColor = ContextCompat.getColor(context, R.color.game_primary)
    private val secondaryColor = ContextCompat.getColor(context, R.color.game_secondary)
    private val accentColor = ContextCompat.getColor(context, R.color.game_accent)
    
    // Game components
    private lateinit var particleSystem: ParticleSystem
    private lateinit var screenShakeEffect: ScreenShakeEffect
    private lateinit var glowEffect: GlowEffect
    private lateinit var soundManager: SoundManager
    private lateinit var gameManager: GameManager
    private lateinit var gameRenderer: GameRenderer
    
    init {
        // Add the callback
        holder.addCallback(this)
        
        // Create the game thread
        gameThread = GameThread(holder, this)
        
        // Make the view focusable to handle events
        isFocusable = true
        
        // Check if sound is enabled in preferences
        val sharedPreferences = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        soundEnabled = sharedPreferences.getBoolean("sound_enabled", true)
        
        // Initialize sound if enabled
        if (soundEnabled) {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        }
    }
    
    // Set game over listener
    fun setOnGameOverListener(listener: (Int) -> Unit) {
        gameOverListener = listener
    }
    
    // Set if the current score is the top score
    fun setIsTopScore(topScore: Boolean) {
        gameManager.isTopScore = topScore
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        // Get screen dimensions
        screenWidth = width
        screenHeight = height
        
        // Initialize game components
        initializeGameComponents()
        
        // Initialize game objects
        initializeGame()
        
        // Initialize game over UI elements
        val buttonWidth = 200f
        val buttonHeight = 60f
        val buttonY = screenHeight / 2f + 150f // Moved down to accommodate high score message
        
        restartButton = RectF(
            screenWidth / 2f - buttonWidth - 20f,
            buttonY,
            screenWidth / 2f - 20f,
            buttonY + buttonHeight
        )
        
        backButton = RectF(
            screenWidth / 2f + 20f,
            buttonY,
            screenWidth / 2f + buttonWidth + 20f,
            buttonY + buttonHeight
        )
        
        // Start the game thread
        gameThread.setRunning(true)
        gameThread.start()
    }
    
    private fun initializeGameComponents() {
        // Initialize game components
        particleSystem = ParticleSystem()
        screenShakeEffect = ScreenShakeEffect()
        glowEffect = GlowEffect()
        soundManager = SoundManager(toneGenerator, soundEnabled)
        
        // Initialize game manager
        gameManager = GameManager(
            screenWidth,
            screenHeight,
            particleSystem,
            screenShakeEffect,
            soundManager
        )
        
        // Initialize game renderer
        gameRenderer = GameRenderer(
            paint,
            particleSystem,
            glowEffect,
            screenShakeEffect
        )
    }
    
    private fun initializeGame() {
        // Initialize game state
        gameManager.initGame()
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handle surface changes if needed
        screenWidth = width
        screenHeight = height
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
        if (!gameManager.gameRunning && event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            
            if (gameManager.lives <= 0) {
                // Game over screen
                if (restartButton.contains(x, y)) {
                    // Restart the game
                    startGame()
                    return true
                } else if (backButton.contains(x, y)) {
                    // Go back to main menu
                    (context as? GameActivity)?.finish()
                    return true
                }
            } else {
                // Start screen
                startGame()
                return true
            }
        }
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if ball is waiting for launch
                if (gameManager.ballWaitingForLaunch) {
                    // Launch the ball
                    gameManager.launchBall()
                    return true
                }
                
                // Move paddle to touch position
                gameManager.movePaddle(event.x)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // Move paddle to touch position
                gameManager.movePaddle(event.x)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    /**
     * Update game state
     */
    fun update(deltaTime: Float) {
        // Update game state
        gameManager.update(deltaTime)
        
        // Update effects
        screenShakeEffect.update(deltaTime)
        glowEffect.update(deltaTime)
        particleSystem.update(deltaTime)
        
        // Check if game over and not reported
        if (gameManager.gameOver && !gameManager.gameOverReported) {
            gameOverListener?.invoke(gameManager.score)
            gameManager.gameOverReported = true
        }
    }
    
    /**
     * Draw the game
     */
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        
        // Apply screen shake effect
        canvas.save()
        canvas.translate(screenShakeEffect.offsetX, screenShakeEffect.offsetY)
        
        // Clear the screen with background color
        canvas.drawColor(backgroundColor)
        
        if (gameManager.gameRunning) {
            // Draw game elements
            gameRenderer.draw(
                canvas,
                backgroundColor,
                gameManager.blocks,
                gameManager.paddle,
                gameManager.ball,
                gameManager.score,
                gameManager.lives,
                gameManager.gameOver,
                gameManager.isTopScore,
                restartButton,
                backButton,
                gameManager.ballWaitingForLaunch
            )
        } else {
            // Draw game over or start screen
            drawGameOverOrStartScreen(canvas)
        }
        
        // Restore canvas after screen shake
        canvas.restore()
    }
    
    /**
     * Draw game over or start screen
     */
    private fun drawGameOverOrStartScreen(canvas: Canvas) {
        paint.color = secondaryColor
        paint.textSize = 60f
        paint.textAlign = Paint.Align.CENTER
        
        if (gameManager.lives <= 0) {
            // Game over screen
            canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - 120f, paint)
            canvas.drawText("Score: ${gameManager.score}", screenWidth / 2f, screenHeight / 2f - 60f, paint)
            
            // Display top score message if applicable
            if (gameManager.isTopScore) {
                paint.color = Color.YELLOW
                paint.textSize = 50f
                canvas.drawText("NEW HIGH SCORE!", screenWidth / 2f, screenHeight / 2f, paint)
                paint.color = secondaryColor
                paint.textSize = 60f
            }
            
            // Draw restart button
            paint.color = primaryColor
            canvas.drawRect(restartButton, paint)
            paint.color = secondaryColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRect(restartButton, paint)
            paint.style = Paint.Style.FILL
            
            // Draw back button
            paint.color = accentColor
            canvas.drawRect(backButton, paint)
            paint.color = secondaryColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRect(backButton, paint)
            paint.style = Paint.Style.FILL
            
            // Draw button text
            paint.color = secondaryColor
            paint.textSize = 30f
            canvas.drawText("Restart", restartButton.centerX(), restartButton.centerY() + 10f, paint)
            canvas.drawText("Back", backButton.centerX(), backButton.centerY() + 10f, paint)
        } else {
            canvas.drawText("BLOCK SHOOTER", screenWidth / 2f, screenHeight / 2f - 60f, paint)
            canvas.drawText("Tap to start", screenWidth / 2f, screenHeight / 2f, paint)
        }
    }
    
    /**
     * Start the game
     */
    fun startGame() {
        // Initialize game
        gameManager.initGame()
        gameManager.gameRunning = true
    }
    
    /**
     * End the game
     */
    fun endGame() {
        // End the game
        gameManager.endGame()
    }
    
    /**
     * Release resources when the view is destroyed
     */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
        soundManager.release()
        particleSystem.cleanup()
        gameManager.cleanup()
    }
} 