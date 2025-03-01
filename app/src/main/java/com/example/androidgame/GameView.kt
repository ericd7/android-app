package com.example.androidgame

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
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    
    private val gameThread: GameThread
    private val paint: Paint = Paint()
    
    // Sound effects
    private var toneGenerator: ToneGenerator? = null
    private val soundEnabled: Boolean
    
    // Screen dimensions
    private var screenWidth = 0
    private var screenHeight = 0
    
    // Game state
    private var gameRunning = false
    private var score = 0
    private var lives = 3
    private var lastBlockAddTime = 0L
    private val blockAddInterval = 10000L // 10 seconds in milliseconds
    private var gameOverReported = false
    
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
    
    // Game objects
    private lateinit var paddle: RectF
    private lateinit var ball: Ball
    private val blocks = mutableListOf<Block>()
    
    // Game settings
    private val paddleHeight = 30f
    private val blockRows = 5
    private val blockCols = 8
    private val blockMargin = 8f
    private val blockHeight = 50f
    private val blockTopOffset = 160f // Increased from 100f to add more padding below score/lives
    private val dangerZone = 150f // Height from bottom where blocks shouldn't be
    
    // Collision handling
    private var lastCollisionTime = 0L
    private val collisionCooldown = 50L // Milliseconds to prevent multiple collisions
    
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
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        // Get screen dimensions
        screenWidth = width
        screenHeight = height
        
        // Initialize game objects
        initializeGame()
        
        // Initialize game over UI elements
        val buttonWidth = 200f
        val buttonHeight = 60f
        val buttonY = screenHeight / 2f + 100f
        
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
    
    private fun initializeGame() {
        // Create paddle
        val paddleWidth = screenWidth / 4f
        val paddleY = screenHeight - 150f
        paddle = RectF(
            screenWidth / 2f - paddleWidth / 2f,
            paddleY,
            screenWidth / 2f + paddleWidth / 2f,
            paddleY + paddleHeight
        )
        
        // Create ball with faster speed
        val ballRadius = 15f
        ball = Ball(
            screenWidth / 2f,
            paddleY - ballRadius - 5f,
            ballRadius,
            16f, // Doubled X velocity
            -16f // Doubled Y velocity
        )
        
        // Create blocks
        createBlocks()
        
        // Reset block add timer
        lastBlockAddTime = System.currentTimeMillis()
        lastCollisionTime = 0L
    }
    
    private fun createBlocks() {
        blocks.clear()
        
        val blockWidth = (screenWidth - (blockCols + 1) * blockMargin) / blockCols
        
        for (row in 0 until blockRows) {
            for (col in 0 until blockCols) {
                val left = (col + 1) * blockMargin + col * blockWidth
                val top = blockTopOffset + row * (blockHeight + blockMargin)
                val right = left + blockWidth
                val bottom = top + blockHeight
                
                // Alternate between primary and accent colors for blocks
                val blockColor = if ((row + col) % 2 == 0) primaryColor else accentColor
                
                blocks.add(Block(RectF(left, top, right, bottom), blockColor))
            }
        }
    }
    
    private fun addNewBlockRow() {
        // Move all existing blocks down
        for (block in blocks) {
            block.rect.top += blockHeight + blockMargin
            block.rect.bottom += blockHeight + blockMargin
        }
        
        // Add new row at the top
        val blockWidth = (screenWidth - (blockCols + 1) * blockMargin) / blockCols
        
        for (col in 0 until blockCols) {
            val left = (col + 1) * blockMargin + col * blockWidth
            val top = blockTopOffset
            val right = left + blockWidth
            val bottom = top + blockHeight
            
            // Alternate colors for new blocks
            val blockColor = if (col % 2 == 0) primaryColor else accentColor
            
            blocks.add(Block(RectF(left, top, right, bottom), blockColor))
        }
        
        // Check if any blocks have reached the danger zone
        checkBlocksPosition()
    }
    
    private fun checkBlocksPosition() {
        // Check if any blocks have reached the danger zone near the paddle
        val dangerY = screenHeight - dangerZone
        
        for (block in blocks) {
            if (block.rect.bottom >= dangerY) {
                // Blocks have reached the bottom - game over
                lives = 0
                endGame()
                return
            }
        }
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
        if (!gameRunning && event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            
            if (lives <= 0) {
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
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Move paddle to touch position
                val touchX = event.x
                val paddleWidth = paddle.right - paddle.left
                
                // Keep paddle within screen bounds
                when {
                    touchX - paddleWidth / 2 < 0 -> {
                        paddle.left = 0f
                        paddle.right = paddleWidth
                    }
                    touchX + paddleWidth / 2 > screenWidth -> {
                        paddle.left = screenWidth - paddleWidth
                        paddle.right = screenWidth.toFloat()
                    }
                    else -> {
                        paddle.left = touchX - paddleWidth / 2
                        paddle.right = touchX + paddleWidth / 2
                    }
                }
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
        
        // Update ball position
        ball.update()
        
        // Check for collisions
        checkCollisions()
        
        // Check if all blocks are broken
        if (blocks.isEmpty()) {
            // Level completed
            createBlocks()
            score += 100 // Bonus for completing level
            
            // Reset block add timer
            lastBlockAddTime = System.currentTimeMillis()
        }
        
        // Check if it's time to add a new row of blocks
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBlockAddTime >= blockAddInterval) {
            addNewBlockRow()
            lastBlockAddTime = currentTime
        }
    }
    
    private fun checkCollisions() {
        // Ball collision with screen edges
        if (ball.x - ball.radius < 0) {
            // Left edge
            ball.x = ball.radius
            ball.velocityX = -ball.velocityX
        } else if (ball.x + ball.radius > screenWidth) {
            // Right edge
            ball.x = screenWidth - ball.radius
            ball.velocityX = -ball.velocityX
        }
        
        if (ball.y - ball.radius < 0) {
            // Top edge
            ball.y = ball.radius
            ball.velocityY = -ball.velocityY
        } else if (ball.y + ball.radius > screenHeight) {
            // Bottom edge - ball lost
            lives--
            if (lives <= 0) {
                endGame()
                // Notify game over with current score
                if (!gameOverReported) {
                    gameOverListener?.invoke(score)
                    gameOverReported = true
                }
            } else {
                // Reset ball position
                ball.x = screenWidth / 2f
                ball.y = paddle.top - ball.radius - 5f
                ball.velocityY = -Math.abs(ball.velocityY) // Ensure upward movement
                
                // Add some randomness to X velocity
                ball.velocityX = if (Random.nextBoolean()) 16f else -16f
            }
        }
        
        // Ball collision with paddle
        if (ball.getBounds().intersect(paddle) && ball.velocityY > 0) {
            // Play paddle hit sound - lower pitch
            if (soundEnabled) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            }
            
            // Calculate bounce angle based on where ball hit the paddle
            val paddleCenter = paddle.left + (paddle.right - paddle.left) / 2
            val hitPoint = ball.x - paddleCenter
            val maxBounceAngle = Math.PI / 3 // 60 degrees
            
            // Normalize hit point to range [-1, 1]
            val normalizedHitPoint = hitPoint / ((paddle.right - paddle.left) / 2)
            
            // Calculate new velocity
            val bounceAngle = normalizedHitPoint * maxBounceAngle
            val speed = Math.sqrt((ball.velocityX * ball.velocityX + ball.velocityY * ball.velocityY).toDouble())
            
            ball.velocityX = (speed * Math.sin(bounceAngle)).toFloat()
            ball.velocityY = (-speed * Math.cos(bounceAngle)).toFloat()
            
            // Ensure ball is above paddle
            ball.y = paddle.top - ball.radius
        }
        
        // Check for collision cooldown
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCollisionTime < collisionCooldown) {
            return
        }
        
        // Find the closest block that intersects with the ball
        var closestBlock: Block? = null
        var minDistance = Float.MAX_VALUE
        
        for (block in blocks) {
            if (ball.getBounds().intersect(block.rect)) {
                val distance = distanceBetween(
                    ball.x, ball.y,
                    block.rect.left + (block.rect.right - block.rect.left) / 2,
                    block.rect.top + (block.rect.bottom - block.rect.top) / 2
                )
                
                if (distance < minDistance) {
                    minDistance = distance
                    closestBlock = block
                }
            }
        }
        
        // Handle collision with the closest block
        closestBlock?.let { block ->
            // Play block hit sound - higher pitch
            if (soundEnabled) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
            }
            
            // Determine collision side
            val ballCenterX = ball.x
            val ballCenterY = ball.y
            val blockCenterX = block.rect.left + (block.rect.right - block.rect.left) / 2
            val blockCenterY = block.rect.top + (block.rect.bottom - block.rect.top) / 2
            
            val dx = ballCenterX - blockCenterX
            val dy = ballCenterY - blockCenterY
            
            // Calculate intersection depths
            val overlapX = if (dx > 0) 
                block.rect.right - (ball.x - ball.radius)
            else
                (ball.x + ball.radius) - block.rect.left
                
            val overlapY = if (dy > 0)
                block.rect.bottom - (ball.y - ball.radius)
            else
                (ball.y + ball.radius) - block.rect.top
            
            // Determine which side was hit based on overlap and velocity
            if (overlapX < overlapY) {
                // Horizontal collision
                ball.velocityX = -ball.velocityX
                
                // Adjust position to prevent sticking
                if (dx > 0) {
                    ball.x = block.rect.right + ball.radius
                } else {
                    ball.x = block.rect.left - ball.radius
                }
            } else {
                // Vertical collision
                ball.velocityY = -ball.velocityY
                
                // Adjust position to prevent sticking
                if (dy > 0) {
                    ball.y = block.rect.bottom + ball.radius
                } else {
                    ball.y = block.rect.top - ball.radius
                }
            }
            
            // Remove block and increase score
            blocks.remove(block)
            score += 10
            
            // Slightly increase ball speed
            val speedMultiplier = 1.01f
            ball.velocityX *= speedMultiplier
            ball.velocityY *= speedMultiplier
            
            // Set collision cooldown
            lastCollisionTime = currentTime
        }
    }
    
    private fun distanceBetween(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
    
    /**
     * Draw the game
     */
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        
        // Clear the screen with background color
        canvas.drawColor(backgroundColor)
        
        if (gameRunning) {
            // Draw paddle
            paint.color = primaryColor
            canvas.drawRect(paddle, paint)
            
            // Draw ball
            paint.color = secondaryColor
            canvas.drawCircle(ball.x, ball.y, ball.radius, paint)
            
            // Draw divider line between score area and blocks
            paint.color = Color.WHITE
            paint.strokeWidth = 2f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(0f, blockTopOffset - 10f, screenWidth.toFloat(), blockTopOffset - 10f, paint)
            paint.style = Paint.Style.FILL
            
            // Draw blocks
            for (block in blocks) {
                paint.color = block.color
                canvas.drawRect(block.rect, paint)
                
                // Draw block border
                paint.color = secondaryColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawRect(block.rect, paint)
                paint.style = Paint.Style.FILL
            }
            
            // Draw danger zone line
            paint.color = Color.RED
            paint.strokeWidth = 3f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(0f, screenHeight - dangerZone, screenWidth.toFloat(), screenHeight - dangerZone, paint)
            paint.style = Paint.Style.FILL
        } else {
            // Draw game over or start screen
            paint.color = secondaryColor
            paint.textSize = 60f
            paint.textAlign = Paint.Align.CENTER
            if (lives <= 0) {
                // Game over screen
                canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - 60f, paint)
                canvas.drawText("Score: $score", screenWidth / 2f, screenHeight / 2f, paint)
                
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
        
        // Draw score and lives with larger font and further down from the top
        paint.color = secondaryColor
        paint.textSize = 55f  // Increased from 40f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Score: $score", 20f, 100f, paint)  // Moved down from 50f to 100f
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Lives: $lives", screenWidth - 20f, 100f, paint)  // Moved down from 50f to 100f
    }
    
    /**
     * Start the game
     */
    fun startGame() {
        if (lives <= 0) {
            // Reset game
            score = 0
            lives = 3
            initializeGame()
        }
        gameRunning = true
        gameOverReported = false
    }
    
    /**
     * End the game
     */
    fun endGame() {
        gameRunning = false
        
        // Report game over if lives are depleted and not already reported
        if (lives <= 0 && !gameOverReported) {
            gameOverListener?.invoke(score)
            gameOverReported = true
        }
    }
    
    /**
     * Release resources when the view is destroyed
     */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
    
    // Ball class
    inner class Ball(
        var x: Float,
        var y: Float,
        val radius: Float,
        var velocityX: Float,
        var velocityY: Float
    ) {
        fun update() {
            x += velocityX
            y += velocityY
        }
        
        fun getBounds(): RectF {
            return RectF(
                x - radius,
                y - radius,
                x + radius,
                y + radius
            )
        }
    }
    
    // Block class
    data class Block(val rect: RectF, val color: Int)
} 