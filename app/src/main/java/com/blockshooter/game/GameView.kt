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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import java.util.concurrent.CopyOnWriteArrayList

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
    private var gameStartTime = 0L
    private var blockAddInterval = 8000L // 8 seconds in milliseconds (initial value)
    private val minBlockAddInterval = 3000L // Minimum interval (3 seconds)
    private val blockAddIntervalDecreaseRate = 750L // Decrease by 750ms every difficulty increase
    private val difficultyIncreaseInterval = 20000L // Increase difficulty every 20 seconds
    private var currentDifficultyLevel = 0
    private var gameOverReported = false
    private var isTopScore = false // Track if current score is the top score
    private var totalRowsAdded = 0 // Track total rows added for proper color staggering
    
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
    private val goldenColor = Color.rgb(255, 215, 0) // Golden color for special blocks
    
    // Rainbow colors for blocks
    private val rainbowColors = arrayOf(
        Color.rgb(255, 0, 0),      // Red
        Color.rgb(255, 127, 0),    // Orange
        Color.rgb(255, 255, 0),    // Yellow
        Color.rgb(0, 255, 0),      // Green
        Color.rgb(0, 0, 255),      // Blue
        Color.rgb(75, 0, 130),     // Indigo
        Color.rgb(148, 0, 211)     // Violet
    )
    
    // Game objects
    private lateinit var paddle: RectF
    private lateinit var ball: Ball
    private val blocks = mutableListOf<Block>()
    
    // Visual effects
    private val blockCornerRadius = 8f
    private val blockShadowOffset = 4f
    private val blockShadowColor = Color.argb(100, 0, 0, 0) // Semi-transparent black for shadows
    private val blockGradientFactor = 0.8f // Factor to darken the bottom of blocks for gradient effect
    private val paddleCornerRadius = 15f // Larger corner radius for paddle
    private val ballGradientRadius = 0.7f // Factor for ball gradient
    
    // Special block glow effect
    private var glowPulseValue = 0f
    private var glowPulseDirection = 1f
    private val glowPulseSpeed = 0.05f
    private val glowPulseMin = 0.6f
    private val glowPulseMax = 1.0f
    
    // Screen shake effect
    private var screenShakeTime = 0
    private var screenShakeIntensity = 0f
    private var screenShakeOffsetX = 0f
    private var screenShakeOffsetY = 0f
    
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
    private val maxBallSpeed = 25f // Maximum ball speed to prevent tunneling
    
    // Particle system
    private val particles = CopyOnWriteArrayList<Particle>()
    private val particleLifespan = 30 // frames
    private val particleSize = 8f
    private val particleCount = 15 // particles per block
    private val particleMaxSpeed = 15f
    
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
        isTopScore = topScore
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
            // Get color for this row from rainbow colors
            val rowColor = rainbowColors[row % rainbowColors.size]
            
            for (col in 0 until blockCols) {
                val left = (col + 1) * blockMargin + col * blockWidth
                val top = blockTopOffset + row * (blockHeight + blockMargin)
                val right = left + blockWidth
                val bottom = top + blockHeight
                
                // No special blocks in initial rows - removed the random check
                val isSpecial = false
                
                // Use rainbow color for this row
                val blockColor = if (isSpecial) goldenColor else rowColor
                
                blocks.add(Block(RectF(left, top, right, bottom), blockColor, isSpecial))
            }
        }
        
        // Reset total rows added since we're starting with a fresh set of blocks
        totalRowsAdded = blockRows
    }
    
    private fun addNewBlockRow() {
        // Move all existing blocks down
        for (block in blocks) {
            block.rect.top += blockHeight + blockMargin
            block.rect.bottom += blockHeight + blockMargin
        }
        
        // Add a new row at the top
        val blockWidth = (screenWidth - (blockCols + 1) * blockMargin) / blockCols
        
        // Get color for this row from rainbow colors
        val rowColor = rainbowColors[totalRowsAdded % rainbowColors.size]
        
        for (col in 0 until blockCols) {
            val left = (col + 1) * blockMargin + col * blockWidth
            val top = blockTopOffset
            val right = left + blockWidth
            val bottom = top + blockHeight
            
            // 5% chance for a special golden block
            val isSpecial = Random.nextInt(20) == 0
            
            // Use rainbow color for this row, or golden for special blocks
            val blockColor = if (isSpecial) goldenColor else rowColor
            
            blocks.add(Block(RectF(left, top, right, bottom), blockColor, isSpecial))
        }
        
        // Increment total rows added
        totalRowsAdded++
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
        
        try {
            // Update screen shake effect
            updateScreenShake()
            
            // Update glow pulse effect for special blocks
            updateGlowPulse()
            
            // Update ball position with continuous collision detection
            updateBallWithCollisionDetection()
            
            // Check if all blocks are broken
            if (blocks.isEmpty()) {
                // Level completed
                createBlocks()
                score += 100 // Bonus for completing level
                
                // Reset block add timer
                lastBlockAddTime = System.currentTimeMillis()
            }
            
            // Update difficulty based on elapsed time
            updateDifficulty()
            
            // Check if it's time to add a new row of blocks
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBlockAddTime >= blockAddInterval) {
                addNewBlockRow()
                lastBlockAddTime = currentTime
            }
            
            // Check if blocks have reached the danger zone
            checkBlocksPosition()
            
            // Update particles
            updateParticles()
        } catch (e: Exception) {
            // If any exception occurs, log it and continue the game
            e.printStackTrace()
        }
    }
    
    /**
     * Update screen shake effect
     */
    private fun updateScreenShake() {
        if (screenShakeTime > 0) {
            screenShakeTime--
            
            // Calculate random offset based on current intensity
            val intensity = screenShakeIntensity * (screenShakeTime / 10f)
            screenShakeOffsetX = (Random.nextFloat() * 2 - 1) * intensity
            screenShakeOffsetY = (Random.nextFloat() * 2 - 1) * intensity
        } else {
            screenShakeOffsetX = 0f
            screenShakeOffsetY = 0f
        }
    }
    
    /**
     * Start screen shake effect
     */
    private fun startScreenShake(intensity: Float = 15f, duration: Int = 10) {
        screenShakeIntensity = intensity
        screenShakeTime = duration
    }
    
    /**
     * Update game difficulty based on elapsed time
     */
    private fun updateDifficulty() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - gameStartTime
        
        // Calculate how many difficulty increases should have occurred
        val newDifficultyLevel = (elapsedTime / difficultyIncreaseInterval).toInt()
        
        // Check if difficulty level has increased
        if (newDifficultyLevel > currentDifficultyLevel) {
            // Difficulty has increased
            currentDifficultyLevel = newDifficultyLevel
        }
        
        // Calculate new block add interval based on difficulty level
        val newInterval = maxOf(
            minBlockAddInterval,
            8000L - (currentDifficultyLevel * blockAddIntervalDecreaseRate)
        )
        
        // Only update if the interval has changed
        if (newInterval < blockAddInterval) {
            blockAddInterval = newInterval
        }
    }
    
    private fun updateBallWithCollisionDetection() {
        // Store original position for collision detection
        val originalX = ball.x
        val originalY = ball.y
        
        // Cap ball speed to prevent tunneling
        val speed = Math.sqrt((ball.velocityX * ball.velocityX + ball.velocityY * ball.velocityY).toDouble()).toFloat()
        if (speed > maxBallSpeed) {
            val scale = maxBallSpeed / speed
            ball.velocityX *= scale
            ball.velocityY *= scale
        }
        
        // Calculate new position
        val newX = ball.x + ball.velocityX
        val newY = ball.y + ball.velocityY
        
        // Check for screen edge collisions first
        checkScreenEdgeCollisions(newX, newY)
        
        // Check for paddle collision
        if (checkPaddleCollision(originalX, originalY, ball.x, ball.y)) {
            return // Ball already handled in paddle collision
        }
        
        // Check for block collisions using ray casting
        if (checkBlockCollisions(originalX, originalY, ball.x, ball.y)) {
            return // Ball already handled in block collision
        }
        
        // If no collisions, update ball position
        ball.x = ball.x + ball.velocityX
        ball.y = ball.y + ball.velocityY
    }
    
    private fun checkScreenEdgeCollisions(newX: Float, newY: Float) {
        // Left edge
        if (newX - ball.radius < 0) {
            ball.x = ball.radius
            ball.velocityX = -ball.velocityX
            return
        }
        
        // Right edge
        if (newX + ball.radius > screenWidth) {
            ball.x = screenWidth - ball.radius
            ball.velocityX = -ball.velocityX
            return
        }
        
        // Top edge
        if (newY - ball.radius < 0) {
            ball.y = ball.radius
            ball.velocityY = -ball.velocityY
            return
        }
        
        // Bottom edge - ball lost
        if (newY + ball.radius > screenHeight) {
            // Create explosion effect when losing a life
            createLifeLostEffect(ball.x, screenHeight - 50f)
            
            // Play sound when losing a life
            if (soundEnabled) {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200)
            }
            
            // Start screen shake effect
            startScreenShake(20f, 15)
            
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
            return
        }
    }
    
    private fun checkPaddleCollision(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
        // Only check for paddle collision if ball is moving downward
        if (ball.velocityY <= 0) return false
        
        // Create a slightly expanded paddle for better collision detection
        val expandedPaddle = RectF(
            paddle.left - ball.radius,
            paddle.top - ball.radius,
            paddle.right + ball.radius,
            paddle.bottom + ball.radius
        )
        
        // Check if the ball's path intersects with the paddle
        if (lineIntersectsRect(startX, startY, endX, endY, expandedPaddle)) {
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
            
            return true
        }
        
        return false
    }
    
    private fun checkBlockCollisions(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
        // Check for collision cooldown
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCollisionTime < collisionCooldown) {
            return false
        }
        
        // Find all blocks that might intersect with the ball's path
        val potentialCollisions = mutableListOf<Pair<Block, Float>>()
        
        try {
            for (block in blocks) {
                // Create a slightly expanded block for better collision detection
                val expandedBlock = RectF(
                    block.rect.left - ball.radius,
                    block.rect.top - ball.radius,
                    block.rect.right + ball.radius,
                    block.rect.bottom + ball.radius
                )
                
                // Check if the ball's path intersects with the block
                if (lineIntersectsRect(startX, startY, endX, endY, expandedBlock)) {
                    // Calculate distance to block center for sorting
                    val blockCenterX = block.rect.left + (block.rect.right - block.rect.left) / 2
                    val blockCenterY = block.rect.top + (block.rect.bottom - block.rect.top) / 2
                    val distance = distanceBetween(startX, startY, blockCenterX, blockCenterY)
                    
                    potentialCollisions.add(Pair(block, distance))
                }
            }
            
            // Sort by distance to find the closest block
            potentialCollisions.sortBy { it.second }
            
            // Handle collision with the closest block
            if (potentialCollisions.isNotEmpty()) {
                val closestBlock = potentialCollisions[0].first
                
                // Play block hit sound - higher pitch
                if (soundEnabled) {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
                }
                
                // Determine collision side
                val ballCenterX = ball.x
                val ballCenterY = ball.y
                val blockCenterX = closestBlock.rect.left + (closestBlock.rect.right - closestBlock.rect.left) / 2
                val blockCenterY = closestBlock.rect.top + (closestBlock.rect.bottom - closestBlock.rect.top) / 2
                
                val dx = ballCenterX - blockCenterX
                val dy = ballCenterY - blockCenterY
                
                // Calculate intersection depths
                val overlapX = if (dx > 0) 
                    closestBlock.rect.right - (ball.x - ball.radius)
                else
                    (ball.x + ball.radius) - closestBlock.rect.left
                    
                val overlapY = if (dy > 0)
                    closestBlock.rect.bottom - (ball.y - ball.radius)
                else
                    (ball.y + ball.radius) - closestBlock.rect.top
                
                // Determine which side was hit based on overlap and velocity
                if (overlapX < overlapY) {
                    // Horizontal collision
                    ball.velocityX = -ball.velocityX
                    
                    // Adjust position to prevent sticking
                    if (dx > 0) {
                        ball.x = closestBlock.rect.right + ball.radius
                    } else {
                        ball.x = closestBlock.rect.left - ball.radius
                    }
                } else {
                    // Vertical collision
                    ball.velocityY = -ball.velocityY
                    
                    // Adjust position to prevent sticking
                    if (dy > 0) {
                        ball.y = closestBlock.rect.bottom + ball.radius
                    } else {
                        ball.y = closestBlock.rect.top - ball.radius
                    }
                }
                
                // Create particles at the block's position before potentially removing it
                createParticles(closestBlock.rect.centerX(), closestBlock.rect.centerY(), closestBlock.color)
                
                // Check if the hit block is a special block
                if (closestBlock.isSpecial) {
                    // Play special block hit sound - different tone
                    if (soundEnabled) {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
                    }
                    
                    // Process the chain reaction of special blocks
                    processSpecialBlockChain(closestBlock)
                } else {
                    // Remove regular block and increase score
                    blocks.remove(closestBlock)
                    score += 10
                }
                
                // Slightly increase ball speed (but not too much to prevent tunneling)
                val speedMultiplier = 1.01f
                ball.velocityX *= speedMultiplier
                ball.velocityY *= speedMultiplier
                
                // Set collision cooldown
                lastCollisionTime = currentTime
                
                return true
            }
        } catch (e: Exception) {
            // If any exception occurs, log it and continue the game
            // In a production app, you would want to log this properly
            e.printStackTrace()
        }
        
        return false
    }
    
    // Helper method to process special block chain reactions
    private fun processSpecialBlockChain(startBlock: Block) {
        try {
            val processedBlocks = mutableSetOf<Block>()
            val blocksToProcess = mutableListOf(startBlock)
            
            while (blocksToProcess.isNotEmpty()) {
                val currentBlock = blocksToProcess.removeAt(0)
                
                // Skip if already processed
                if (currentBlock in processedBlocks) continue
                
                // Mark as processed
                processedBlocks.add(currentBlock)
                
                // Find blocks in the expanded area of effect
                val affectedBlocks = mutableListOf<Block>()
                val blocksToCheck = ArrayList(blocks) // Create a copy to avoid concurrent modification
                
                for (block in blocksToCheck) {
                    if (block != currentBlock && block !in processedBlocks && isBlockInSpecialRange(currentBlock, block)) {
                        affectedBlocks.add(block)
                        
                        // If this is another special block, add it to the processing queue
                        if (block.isSpecial) {
                            blocksToProcess.add(block)
                        }
                    }
                }
                
                // Remove the current block and add score
                blocks.remove(currentBlock)
                score += 10
                
                // Add bonus points for special block
                if (currentBlock.isSpecial) {
                    score += 20
                    
                    // Create enhanced particle effect for special blocks
                    // Golden explosion with more particles
                    for (i in 0 until 3) {
                        createParticles(currentBlock.rect.centerX(), currentBlock.rect.centerY(), goldenColor)
                    }
                    
                    // Add screen shake for special block destruction
                    startScreenShake(10f, 8)
                    
                    // Play chain reaction sound if not the initial block
                    if (currentBlock != startBlock && soundEnabled) {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
                    }
                } else {
                    // Regular particle effect for normal blocks
                    createParticles(currentBlock.rect.centerX(), currentBlock.rect.centerY(), currentBlock.color)
                }
                
                // Remove affected blocks and add score
                for (block in affectedBlocks) {
                    if (block in blocks) { // Check if still in the list
                        // Create particles for each affected block
                        createParticles(block.rect.centerX(), block.rect.centerY(), block.color)
                        
                        blocks.remove(block)
                        score += 10
                    }
                }
            }
        } catch (e: Exception) {
            // If any exception occurs, log it and continue the game
            e.printStackTrace()
        }
    }
    
    // Helper method to determine if a block is within the special block's expanded range
    private fun isBlockInSpecialRange(specialBlock: Block, otherBlock: Block): Boolean {
        try {
            // Get block dimensions
            val blockWidth = specialBlock.rect.width()
            val blockHeight = specialBlock.rect.height()
            
            // Get centers of blocks
            val center1X = specialBlock.rect.left + blockWidth / 2
            val center1Y = specialBlock.rect.top + blockHeight / 2
            val center2X = otherBlock.rect.left + blockWidth / 2
            val center2Y = otherBlock.rect.top + blockHeight / 2
            
            // Calculate distance between centers
            val distanceX = Math.abs(center1X - center2X)
            val distanceY = Math.abs(center1Y - center2Y)
            
            // Check if block is within 2 blocks distance (horizontally and vertically)
            return distanceX <= blockWidth * 2.1f && distanceY <= blockHeight * 2.1f
        } catch (e: Exception) {
            // If any exception occurs, return false to avoid freezing
            e.printStackTrace()
            return false
        }
    }
    
    // Helper method to determine if two blocks are adjacent (for regular adjacency checks)
    private fun isAdjacent(block1: Block, block2: Block): Boolean {
        // Get block dimensions
        val blockWidth = block1.rect.width()
        val blockHeight = block1.rect.height()
        
        // Get centers of blocks
        val center1X = block1.rect.left + blockWidth / 2
        val center1Y = block1.rect.top + blockHeight / 2
        val center2X = block2.rect.left + blockWidth / 2
        val center2Y = block2.rect.top + blockHeight / 2
        
        // Calculate distance between centers
        val distanceX = Math.abs(center1X - center2X)
        val distanceY = Math.abs(center1Y - center2Y)
        
        // Blocks are adjacent if they are one block width/height away (including diagonals)
        return distanceX <= blockWidth * 1.1f && distanceY <= blockHeight * 1.1f
    }
    
    // Helper method to check if a line intersects with a rectangle
    private fun lineIntersectsRect(x1: Float, y1: Float, x2: Float, y2: Float, rect: RectF): Boolean {
        // Check if either endpoint is inside the rectangle
        if (rect.contains(x1, y1) || rect.contains(x2, y2)) {
            return true
        }
        
        // Check if the line intersects any of the rectangle's edges
        return lineIntersectsLine(x1, y1, x2, y2, rect.left, rect.top, rect.right, rect.top) || // Top edge
               lineIntersectsLine(x1, y1, x2, y2, rect.right, rect.top, rect.right, rect.bottom) || // Right edge
               lineIntersectsLine(x1, y1, x2, y2, rect.left, rect.bottom, rect.right, rect.bottom) || // Bottom edge
               lineIntersectsLine(x1, y1, x2, y2, rect.left, rect.top, rect.left, rect.bottom) // Left edge
    }
    
    // Helper method to check if two line segments intersect
    private fun lineIntersectsLine(x1: Float, y1: Float, x2: Float, y2: Float, 
                                  x3: Float, y3: Float, x4: Float, y4: Float): Boolean {
        // Calculate the denominator first to check for division by zero
        val denominator = ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1))
        
        // If denominator is zero, lines are parallel or collinear
        if (abs(denominator) < 0.0001f) {
            // Check if the lines are collinear and overlapping
            // This is a simplified check - we'll just return false to avoid freezing
            return false
        }
        
        // Calculate the direction of the lines
        val uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denominator
        val uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denominator
        
        // If uA and uB are between 0-1, lines are colliding
        return uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1
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
        
        // Apply screen shake effect
        canvas.save()
        canvas.translate(screenShakeOffsetX, screenShakeOffsetY)
        
        // Clear the screen with background color
        canvas.drawColor(backgroundColor)
        
        if (gameRunning) {
            // Draw paddle with 3D effect
            // Draw shadow first
            paint.color = blockShadowColor
            val paddleShadowRect = RectF(
                paddle.left + blockShadowOffset,
                paddle.top + blockShadowOffset,
                paddle.right + blockShadowOffset,
                paddle.bottom + blockShadowOffset
            )
            canvas.drawRoundRect(paddleShadowRect, paddleCornerRadius, paddleCornerRadius, paint)
            
            // Draw main paddle
            paint.color = Color.WHITE
            canvas.drawRoundRect(paddle, paddleCornerRadius, paddleCornerRadius, paint)
            
            // Draw gradient on paddle
            val paddleGradientRect = RectF(
                paddle.left,
                paddle.top + paddle.height() / 2,
                paddle.right,
                paddle.bottom
            )
            paint.color = Color.rgb(200, 200, 200) // Slightly darker white for bottom half
            canvas.drawRoundRect(paddleGradientRect, 0f, 0f, paint)
            
            // Draw highlight on top edge of paddle
            paint.color = Color.argb(100, 255, 255, 255) // Semi-transparent white
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            val paddleHighlightRect = RectF(
                paddle.left + 3f,
                paddle.top + 3f,
                paddle.right - 3f,
                paddle.top + 8f
            )
            canvas.drawRoundRect(paddleHighlightRect, paddleCornerRadius, paddleCornerRadius, paint)
            paint.style = Paint.Style.FILL
            
            // Draw ball
            // Draw ball shadow
            paint.color = blockShadowColor
            canvas.drawCircle(ball.x + blockShadowOffset/2, ball.y + blockShadowOffset/2, ball.radius, paint)
            
            // Draw main ball
            paint.color = secondaryColor
            canvas.drawCircle(ball.x, ball.y, ball.radius, paint)
            
            // Draw ball gradient (darker bottom half)
            paint.color = Color.argb(100, 0, 0, 0) // Semi-transparent black
            val ballBottom = RectF(
                ball.x - ball.radius,
                ball.y,
                ball.x + ball.radius,
                ball.y + ball.radius
            )
            canvas.drawArc(ballBottom, 0f, 180f, true, paint)
            
            // Draw ball highlight (top-left quadrant)
            paint.color = Color.argb(80, 255, 255, 255) // Semi-transparent white
            canvas.drawCircle(
                ball.x - ball.radius * 0.3f,
                ball.y - ball.radius * 0.3f,
                ball.radius * 0.4f,
                paint
            )
            
            // Draw divider line between score area and blocks
            paint.color = Color.WHITE
            paint.strokeWidth = 2f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(0f, blockTopOffset - 10f, screenWidth.toFloat(), blockTopOffset - 10f, paint)
            paint.style = Paint.Style.FILL
            
            // Draw blocks with 3D effect
            for (block in blocks) {
                // Draw shadow first
                paint.color = blockShadowColor
                val shadowRect = RectF(
                    block.rect.left + blockShadowOffset,
                    block.rect.top + blockShadowOffset,
                    block.rect.right + blockShadowOffset,
                    block.rect.bottom + blockShadowOffset
                )
                canvas.drawRoundRect(shadowRect, blockCornerRadius, blockCornerRadius, paint)
                
                // Draw main block
                paint.color = block.color
                canvas.drawRoundRect(block.rect, blockCornerRadius, blockCornerRadius, paint)
                
                // Draw gradient effect (darker bottom half)
                val gradientRect = RectF(
                    block.rect.left,
                    block.rect.top + block.rect.height() / 2,
                    block.rect.right,
                    block.rect.bottom
                )
                paint.color = Color.argb(
                    Color.alpha(block.color),
                    (Color.red(block.color) * blockGradientFactor).toInt(),
                    (Color.green(block.color) * blockGradientFactor).toInt(),
                    (Color.blue(block.color) * blockGradientFactor).toInt()
                )
                canvas.drawRoundRect(gradientRect, 0f, 0f, paint)
                
                // Draw highlight on top edge
                paint.color = Color.argb(100, 255, 255, 255) // Semi-transparent white
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                val highlightRect = RectF(
                    block.rect.left + 2f,
                    block.rect.top + 2f,
                    block.rect.right - 2f,
                    block.rect.top + 5f
                )
                canvas.drawRoundRect(highlightRect, blockCornerRadius, blockCornerRadius, paint)
                paint.style = Paint.Style.FILL
                
                // Draw pulsing glow effect for special blocks
                if (block.isSpecial) {
                    // Draw outer glow
                    val glowSize = 8f * glowPulseValue
                    val glowRect = RectF(
                        block.rect.left - glowSize,
                        block.rect.top - glowSize,
                        block.rect.right + glowSize,
                        block.rect.bottom + glowSize
                    )
                    
                    // Create a radial gradient for the glow
                    val glowAlpha = (100 * glowPulseValue).toInt()
                    paint.color = Color.argb(glowAlpha, 255, 215, 0) // Golden glow with pulsing alpha
                    canvas.drawRoundRect(glowRect, blockCornerRadius + glowSize, blockCornerRadius + glowSize, paint)
                }
            }
            
            // Draw danger zone line
            paint.color = Color.WHITE
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
                canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - 120f, paint)
                canvas.drawText("Score: $score", screenWidth / 2f, screenHeight / 2f - 60f, paint)
                
                // Display top score message if applicable
                if (isTopScore) {
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
        
        // Draw score and lives with larger font and further down from the top
        paint.color = secondaryColor
        paint.textSize = 55f  // Increased from 40f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Score: $score", 20f, 100f, paint)  // Moved down from 50f to 100f
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Lives: $lives", screenWidth - 20f, 100f, paint)  // Moved down from 50f to 100f
        
        // Draw particles
        for (particle in particles) {
            particle.draw(canvas, paint)
        }
        
        // Restore canvas after screen shake
        canvas.restore()
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
        
        // Reset game timers and difficulty
        gameStartTime = System.currentTimeMillis()
        lastBlockAddTime = gameStartTime
        blockAddInterval = 8000L // Reset to initial interval
        currentDifficultyLevel = 0
        isTopScore = false // Reset top score flag
        totalRowsAdded = 0 // Reset total rows added
        
        // Clear any lingering particles
        particles.clear()
        
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
    data class Block(
        val rect: RectF, 
        val color: Int,
        val isSpecial: Boolean = false
    )
    
    /**
     * Particle class for visual effects
     */
    private inner class Particle(
        var x: Float,
        var y: Float,
        var velocityX: Float,
        var velocityY: Float,
        var color: Int,
        var life: Int = particleLifespan
    ) {
        fun update() {
            x += velocityX
            y += velocityY
            velocityY += 0.5f // gravity
            velocityX *= 0.95f // air resistance
            life--
            
            // Ensure particles with very low velocity still die
            if (Math.abs(velocityX) < 0.1f && Math.abs(velocityY) < 0.1f) {
                life = Math.min(life, 5) // Force particle to die soon if it's barely moving
            }
        }
        
        fun draw(canvas: Canvas, paint: Paint) {
            // Fade out as life decreases
            val alpha = (255 * (life.toFloat() / particleLifespan)).toInt()
            paint.color = Color.argb(
                alpha,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
            canvas.drawCircle(x, y, particleSize * (life.toFloat() / particleLifespan), paint)
        }
    }
    
    /**
     * Create particles at the given position with the given color
     */
    private fun createParticles(x: Float, y: Float, color: Int) {
        for (i in 0 until particleCount) {
            val angle = Random.nextDouble(0.0, Math.PI * 2).toFloat()
            // Ensure minimum speed to prevent stationary particles
            val speed = Random.nextFloat() * particleMaxSpeed + 2f
            val velocityX = Math.cos(angle.toDouble()).toFloat() * speed
            val velocityY = Math.sin(angle.toDouble()).toFloat() * speed
            particles.add(Particle(x, y, velocityX, velocityY, color))
        }
    }
    
    /**
     * Update all particles
     */
    private fun updateParticles() {
        try {
            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val particle = iterator.next()
                particle.update()
                if (particle.life <= 0) {
                    iterator.remove()
                }
            }
            
            // Safety check: clear all particles if there are too many
            if (particles.size > 500) {
                particles.clear()
            }
        } catch (e: Exception) {
            // If any exception occurs, clear all particles to prevent issues
            particles.clear()
            e.printStackTrace()
        }
    }
    
    /**
     * Create a special particle effect when the player loses a life
     */
    private fun createLifeLostEffect(x: Float, y: Float) {
        // Create red explosion particles
        val redColor = Color.rgb(255, 50, 50)
        
        // Create more particles for a bigger explosion
        for (i in 0 until 3) {
            for (j in 0 until particleCount) {
                val angle = Random.nextDouble(0.0, Math.PI * 2).toFloat()
                val speed = Random.nextFloat() * particleMaxSpeed * 1.5f + 3f // Ensure minimum speed
                val velocityX = Math.cos(angle.toDouble()).toFloat() * speed
                val velocityY = Math.sin(angle.toDouble()).toFloat() * speed
                
                // Create particles with longer lifespan but not too long
                particles.add(Particle(x, y, velocityX, velocityY, redColor, particleLifespan * 2))
            }
        }
    }
    
    /**
     * Update the pulsing glow effect for special blocks
     */
    private fun updateGlowPulse() {
        glowPulseValue += glowPulseDirection * glowPulseSpeed
        
        if (glowPulseValue >= glowPulseMax) {
            glowPulseValue = glowPulseMax
            glowPulseDirection = -1f
        } else if (glowPulseValue <= glowPulseMin) {
            glowPulseValue = glowPulseMin
            glowPulseDirection = 1f
        }
    }
} 