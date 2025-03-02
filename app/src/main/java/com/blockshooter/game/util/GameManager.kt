package com.blockshooter.game.util

import android.graphics.Color
import android.graphics.RectF
import com.blockshooter.game.effects.ParticleSystem
import com.blockshooter.game.effects.ScreenShakeEffect
import com.blockshooter.game.effects.SoundManager
import com.blockshooter.game.model.Ball
import com.blockshooter.game.model.Block
import kotlin.math.abs
import kotlin.random.Random

/**
 * Manages the game state and logic.
 */
class GameManager(
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val particleSystem: ParticleSystem,
    private val screenShakeEffect: ScreenShakeEffect,
    private val soundManager: SoundManager
) {
    // Game state
    var gameRunning = false
    var score = 0
    var lives = 3
    var gameOver = false
    var isTopScore = false
    var gameOverReported = false
    
    // Game objects
    lateinit var paddle: RectF
    lateinit var ball: Ball
    val blocks = mutableListOf<Block>()
    private val blocksToRemove = mutableListOf<Block>() // List to batch block removals
    
    // Game settings
    private val paddleHeight = 30f
    private val paddleWidth = 200f
    private val ballRadius = 15f
    private val blockRows = 3
    private val blockCols = 6
    private val blockHeight = 40f
    private val blockMargin = 8f
    private val blockTopOffset = 100f
    private val dangerZone = 200f // Distance from bottom where blocks cause game over
    
    // Game timing
    private var lastBlockAddTime = 0L
    private var gameStartTime = 0L
    private var blockAddInterval = 8000L // 8 seconds in milliseconds (initial value)
    private val minBlockAddInterval = 3000L // Minimum interval (3 seconds)
    private val blockAddIntervalDecreaseRate = 750L // Decrease by 750ms every difficulty increase
    private val difficultyIncreaseInterval = 20000L // Increase difficulty every 20 seconds
    private var currentDifficultyLevel = 0
    private var totalRowsAdded = 0 // Track total rows added for proper color staggering
    
    // Collision handling
    private var lastCollisionTime = 0L
    private val collisionCooldown = 50L // Milliseconds between collision checks
    
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
    
    // Special block color
    private val specialBlockColor = Color.rgb(240, 248, 255) // Alice Blue (slightly blue-tinted white)
    
    /**
     * Initializes the game.
     */
    fun initGame() {
        // Reset game state
        gameRunning = true
        score = 0
        lives = 3
        gameOver = false
        isTopScore = false
        gameOverReported = false
        currentDifficultyLevel = 0
        
        // Create paddle
        val paddleLeft = (screenWidth - paddleWidth) / 2
        val paddleTop = screenHeight - 100f
        paddle = RectF(paddleLeft, paddleTop, paddleLeft + paddleWidth, paddleTop + paddleHeight)
        
        // Create ball
        ball = Ball(
            screenWidth / 2f,
            paddle.top - ballRadius - 5f,
            ballRadius,
            if (Random.nextBoolean()) 800f else -800f, // Random initial X direction
            -800f // Initial Y velocity (upward)
        )
        
        // Create blocks
        createBlocks()
        
        // Reset block add timer
        lastBlockAddTime = System.currentTimeMillis()
        gameStartTime = System.currentTimeMillis()
        lastCollisionTime = 0L
    }
    
    /**
     * Creates the initial set of blocks.
     */
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
                
                // Use rainbow color for this row, or white for special blocks
                val blockColor = if (isSpecial) specialBlockColor else rowColor
                
                blocks.add(Block(RectF(left, top, right, bottom), blockColor, isSpecial))
            }
        }
        
        // Reset total rows added since we're starting with a fresh set of blocks
        totalRowsAdded = blockRows
    }
    
    /**
     * Updates the game state.
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     */
    fun update(deltaTime: Float) {
        if (!gameRunning || gameOver) return
        
        // Update ball position
        ball.update(deltaTime)
        
        // Check for collisions
        checkCollisions()
        
        // Check if it's time to add a new row of blocks
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBlockAddTime > blockAddInterval) {
            addNewBlockRow()
            lastBlockAddTime = currentTime
            
            // Play level up sound
            soundManager.playLevelUpSound()
        }
        
        // Check if blocks have reached the danger zone
        checkBlocksPosition()
        
        // Increase difficulty over time
        updateDifficulty(currentTime)
        
        // Remove destroyed blocks in batch
        if (blocksToRemove.isNotEmpty()) {
            blocks.removeAll(blocksToRemove)
            blocksToRemove.clear()
        }
    }
    
    /**
     * Adds a new row of blocks at the top of the screen.
     */
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
            
            // Use rainbow color for this row, or white for special blocks
            val blockColor = if (isSpecial) specialBlockColor else rowColor
            
            blocks.add(Block(RectF(left, top, right, bottom), blockColor, isSpecial))
        }
        
        // Increment total rows added
        totalRowsAdded++
    }
    
    /**
     * Checks if blocks have reached the danger zone near the paddle.
     */
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
    
    /**
     * Updates the difficulty level based on elapsed time.
     * 
     * @param currentTime The current time in milliseconds
     */
    private fun updateDifficulty(currentTime: Long) {
        val elapsedTime = currentTime - gameStartTime
        val newDifficultyLevel = (elapsedTime / difficultyIncreaseInterval).toInt()
        
        if (newDifficultyLevel > currentDifficultyLevel) {
            currentDifficultyLevel = newDifficultyLevel
            
            // Decrease block add interval (make game harder)
            blockAddInterval = (blockAddInterval - blockAddIntervalDecreaseRate)
                .coerceAtLeast(minBlockAddInterval)
        }
    }
    
    /**
     * Checks for collisions between the ball and other game objects.
     */
    private fun checkCollisions() {
        // Store current ball position
        val startX = ball.x
        val startY = ball.y
        
        // Calculate new position - use a small fraction of deltaTime for look-ahead
        val lookAheadFactor = 0.016f // Equivalent to about one frame at 60fps
        val newX = ball.x + ball.velocityX * lookAheadFactor
        val newY = ball.y + ball.velocityY * lookAheadFactor
        
        // Check for wall collisions first (most common and cheapest to check)
        if (checkWallCollisions(newX, newY)) {
            return
        }
        
        // Check for paddle collision
        if (checkPaddleCollision(startX, startY, newX, newY)) {
            return
        }
        
        // Check for block collisions (most expensive)
        checkBlockCollisions(startX, startY, newX, newY)
    }
    
    /**
     * Checks for collisions between the ball and walls.
     * 
     * @param newX The ball's new X position
     * @param newY The ball's new Y position
     * @return True if a collision occurred, false otherwise
     */
    private fun checkWallCollisions(newX: Float, newY: Float): Boolean {
        // Left edge
        if (newX - ball.radius < 0) {
            ball.x = ball.radius
            ball.velocityX = -ball.velocityX
            return true
        }
        
        // Right edge
        if (newX + ball.radius > screenWidth) {
            ball.x = screenWidth - ball.radius
            ball.velocityX = -ball.velocityX
            return true
        }
        
        // Top edge
        if (newY - ball.radius < 0) {
            ball.y = ball.radius
            ball.velocityY = -ball.velocityY
            return true
        }
        
        // Bottom edge - ball lost
        if (newY + ball.radius > screenHeight) {
            // Create explosion effect when losing a life
            particleSystem.createLifeLostEffect(ball.x, screenHeight - 50f)
            
            // Play sound when losing a life
            soundManager.playBallLostSound()
            
            // Start screen shake effect
            screenShakeEffect.startScreenShake(20f, 15)
            
            lives--
            if (lives <= 0) {
                endGame()
            } else {
                // Reset ball position
                ball.x = screenWidth / 2f
                ball.y = paddle.top - ball.radius - 5f
                ball.velocityY = -Math.abs(ball.velocityY) // Ensure upward movement
                
                // Reset X velocity to a random direction
                ball.velocityX = if (Random.nextBoolean()) 800f else -800f
            }
            return true
        }
        
        return false
    }
    
    /**
     * Checks for collisions between the ball and the paddle.
     * 
     * @param startX The ball's starting X position
     * @param startY The ball's starting Y position
     * @param endX The ball's ending X position
     * @param endY The ball's ending Y position
     * @return True if a collision occurred, false otherwise
     */
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
        if (CollisionUtils.lineIntersectsRect(startX, startY, endX, endY, expandedPaddle)) {
            // Play paddle hit sound
            soundManager.playPaddleHitSound()
            
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
    
    /**
     * Checks for collisions between the ball and blocks.
     * 
     * @param startX The ball's starting X position
     * @param startY The ball's starting Y position
     * @param endX The ball's ending X position
     * @param endY The ball's ending Y position
     * @return True if a collision occurred, false otherwise
     */
    private fun checkBlockCollisions(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
        // Check for collision cooldown - using a fixed cooldown is fine since we're just
        // preventing multiple collisions in the same frame
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCollisionTime < collisionCooldown) {
            return false
        }
        
        // Quick check if ball is in the blocks area to avoid unnecessary calculations
        if (ball.y < blockTopOffset - ball.radius || ball.y > screenHeight - dangerZone + blockHeight) {
            return false
        }
        
        // Find closest block that might intersect with the ball's path
        var closestBlock: Block? = null
        var closestDistance = Float.MAX_VALUE
        
        try {
            for (block in blocks) {
                if (block.isDestroyed) continue
                
                // Quick bounding box check before more expensive intersection test
                if (!isInBlockArea(ball, block)) continue
                
                // Check if the ball's path intersects with this block
                if (CollisionUtils.lineIntersectsRect(startX, startY, endX, endY, block.rect)) {
                    // Calculate distance to block center for sorting
                    val blockCenterX = block.rect.left + (block.rect.right - block.rect.left) / 2
                    val blockCenterY = block.rect.top + (block.rect.bottom - block.rect.top) / 2
                    val distance = (startX - blockCenterX) * (startX - blockCenterX) +
                            (startY - blockCenterY) * (startY - blockCenterY)
                    
                    if (distance < closestDistance) {
                        closestDistance = distance
                        closestBlock = block
                    }
                }
            }
            
            // Handle the closest collision
            closestBlock?.let { block ->
                // Determine which side of the block was hit
                val side = CollisionUtils.getCollisionSide(ball, block)
                
                // Bounce the ball based on which side was hit
                when (side) {
                    "top", "bottom" -> ball.velocityY = -ball.velocityY
                    "left", "right" -> ball.velocityX = -ball.velocityX
                }
                
                // Create particles at the collision point
                particleSystem.createParticles(ball.x, ball.y, block.color)
                
                // Play sound
                if (block.isSpecial) {
                    soundManager.playSpecialBlockHitSound()
                } else {
                    soundManager.playBlockHitSound()
                }
                
                // Mark block as destroyed
                block.isDestroyed = true
                blocksToRemove.add(block)
                
                // Add score
                score += if (block.isSpecial) 50 else 10
                
                // Start screen shake for special blocks
                if (block.isSpecial) {
                    screenShakeEffect.startScreenShake(10f, 10)
                    
                    // Chain reaction - destroy adjacent blocks
                    destroyAdjacentBlocks(block)
                }
                
                // Update collision time
                lastCollisionTime = currentTime
                
                return true
            }
        } catch (e: Exception) {
            // If any exception occurs, just continue
            e.printStackTrace()
        }
        
        return false
    }
    
    /**
     * Quick check if ball is in the area of a block
     */
    private fun isInBlockArea(ball: Ball, block: Block): Boolean {
        // Expanded block area by ball radius for collision check
        return ball.x + ball.radius >= block.rect.left &&
               ball.x - ball.radius <= block.rect.right &&
               ball.y + ball.radius >= block.rect.top &&
               ball.y - ball.radius <= block.rect.bottom
    }
    
    /**
     * Destroys blocks adjacent to the given block (chain reaction for special blocks).
     * 
     * @param sourceBlock The block that triggered the chain reaction
     */
    private fun destroyAdjacentBlocks(sourceBlock: Block) {
        val adjacentBlocks = mutableListOf<Block>()
        
        // Find blocks that are adjacent to the source block
        for (block in blocks) {
            if (block.isDestroyed || block === sourceBlock) continue
            
            // Check if this block is adjacent to the source block
            if (blocksAreAdjacent(sourceBlock.rect, block.rect)) {
                adjacentBlocks.add(block)
            }
        }
        
        // Destroy adjacent blocks
        for (block in adjacentBlocks) {
            // Mark block as destroyed
            block.isDestroyed = true
            blocksToRemove.add(block)
            
            // Create particles
            val blockCenterX = block.rect.left + (block.rect.right - block.rect.left) / 2
            val blockCenterY = block.rect.top + (block.rect.bottom - block.rect.top) / 2
            particleSystem.createParticles(blockCenterX, blockCenterY, block.color)
            
            // Add score
            score += if (block.isSpecial) 50 else 10
        }
    }
    
    /**
     * Checks if two blocks are adjacent to each other.
     * 
     * @param rect1 The first block's rectangle
     * @param rect2 The second block's rectangle
     * @return True if the blocks are adjacent, false otherwise
     */
    private fun blocksAreAdjacent(rect1: RectF, rect2: RectF): Boolean {
        // Calculate the centers of the blocks
        val center1X = rect1.left + (rect1.right - rect1.left) / 2
        val center1Y = rect1.top + (rect1.bottom - rect1.top) / 2
        val center2X = rect2.left + (rect2.right - rect2.left) / 2
        val center2Y = rect2.top + (rect2.bottom - rect2.top) / 2
        
        // Calculate the distance between the centers
        val distanceX = abs(center1X - center2X)
        val distanceY = abs(center1Y - center2Y)
        
        // Calculate the sum of half widths and half heights
        val sumHalfWidths = (rect1.right - rect1.left) / 2 + (rect2.right - rect2.left) / 2
        val sumHalfHeights = (rect1.bottom - rect1.top) / 2 + (rect2.bottom - rect2.top) / 2
        
        // Check if the blocks are adjacent (allowing for a small gap)
        return distanceX <= sumHalfWidths * 1.2f && distanceY <= sumHalfHeights * 1.2f
    }
    
    /**
     * Moves the paddle to the specified x-coordinate.
     * 
     * @param x The x-coordinate to move the paddle to
     */
    fun movePaddle(x: Float) {
        val halfPaddleWidth = paddleWidth / 2
        val newX = x.coerceIn(halfPaddleWidth, screenWidth - halfPaddleWidth)
        
        paddle.left = newX - halfPaddleWidth
        paddle.right = newX + halfPaddleWidth
    }
    
    /**
     * Ends the game.
     */
    fun endGame() {
        gameRunning = false
        gameOver = true
        
        // Play game over sound
        soundManager.playGameOverSound()
    }
    
    /**
     * Cleans up resources.
     */
    fun cleanup() {
        // Nothing to clean up for now
    }
} 