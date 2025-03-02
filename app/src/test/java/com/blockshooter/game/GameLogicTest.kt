package com.blockshooter.game

import android.content.Context
import android.graphics.RectF
import com.blockshooter.game.model.Block
import com.blockshooter.game.util.GameManager
import com.blockshooter.game.effects.ParticleSystem
import com.blockshooter.game.effects.ScreenShakeEffect
import com.blockshooter.game.effects.SoundManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Field
import java.lang.reflect.Method

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GameLogicTest {

    @Mock
    private lateinit var particleSystem: ParticleSystem
    
    @Mock
    private lateinit var screenShakeEffect: ScreenShakeEffect
    
    @Mock
    private lateinit var soundManager: SoundManager
    
    private lateinit var gameManager: GameManager
    private lateinit var endGameMethod: Method
    private lateinit var addNewBlockRowMethod: Method
    private lateinit var checkBlocksPositionMethod: Method
    
    // Screen dimensions for testing
    private val screenWidth = 800
    private val screenHeight = 1200
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Create a GameManager instance with mocked dependencies
        gameManager = GameManager(screenWidth, screenHeight, particleSystem, screenShakeEffect, soundManager)
        
        // Get access to private methods via reflection
        endGameMethod = GameManager::class.java.getDeclaredMethod("endGame")
        endGameMethod.isAccessible = true
        
        addNewBlockRowMethod = GameManager::class.java.getDeclaredMethod("addNewBlockRow")
        addNewBlockRowMethod.isAccessible = true
        
        checkBlocksPositionMethod = GameManager::class.java.getDeclaredMethod("checkBlocksPosition")
        checkBlocksPositionMethod.isAccessible = true
    }
    
    /**
     * Helper method to simulate what GameView.startGame() does
     */
    private fun startGame() {
        gameManager.initGame()
        gameManager.gameRunning = true
    }
    
    /**
     * Helper method to simulate losing a life
     */
    private fun loseLife() {
        gameManager.lives--
        if (gameManager.lives <= 0) {
            // Call the endGame method via reflection
            endGameMethod.invoke(gameManager)
        } else {
            // Reset ball position (similar to what happens in GameManager)
            gameManager.ball.x = screenWidth / 2f
            gameManager.ball.y = gameManager.paddle.top - gameManager.ball.radius - 5f
            gameManager.ball.velocityY = -Math.abs(gameManager.ball.velocityY)
        }
    }
    
    /**
     * Helper method to add a new row of blocks
     */
    private fun addNewBlockRow() {
        addNewBlockRowMethod.invoke(gameManager)
    }
    
    @Test
    fun `test game initialization`() {
        // Verify initial game state
        assertFalse(gameManager.gameRunning)
        assertEquals(0, gameManager.score)
        assertEquals(3, gameManager.lives)
        assertFalse(gameManager.gameOver)
        
        // Start the game
        startGame()
        
        // Verify game is running
        assertTrue(gameManager.gameRunning)
        assertEquals(0, gameManager.score)
        assertEquals(3, gameManager.lives)
        assertFalse(gameManager.gameOver)
        
        // Verify blocks were created
        assertTrue(gameManager.blocks.isNotEmpty())
    }
    
    @Test
    fun `test game over`() {
        // Start the game
        startGame()
        
        // Set lives to 1
        gameManager.lives = 1
        
        // Lose a life
        loseLife()
        
        // Verify game is over
        assertTrue(gameManager.gameOver)
        assertFalse(gameManager.gameRunning)
    }
    
    @Test
    fun `test score increases when block is destroyed`() {
        // Start the game
        startGame()
        
        // Clear existing blocks
        gameManager.blocks.clear()
        
        // Add a test block
        val testBlock = Block(RectF(100f, 100f, 150f, 150f), 0)
        gameManager.blocks.add(testBlock)
        
        // Initial score
        val initialScore = gameManager.score
        
        // Mark the block as destroyed
        testBlock.isDestroyed = true
        
        // Update the game to process the destroyed block
        gameManager.update(0.016f)
        
        // Verify score increased
        assertTrue(gameManager.score > initialScore)
        
        // Verify block was removed
        assertTrue(gameManager.blocks.isEmpty())
    }
    
    @Test
    fun `test special block gives bonus points`() {
        // Start the game
        startGame()
        
        // Clear existing blocks
        gameManager.blocks.clear()
        
        // Add a regular block and a special block
        val regularBlock = Block(RectF(100f, 100f, 150f, 150f), 0)
        val specialBlock = Block(RectF(200f, 200f, 250f, 250f), 0, true)
        
        gameManager.blocks.add(regularBlock)
        gameManager.blocks.add(specialBlock)
        
        // Mark blocks as destroyed
        regularBlock.isDestroyed = true
        
        // Update to process the regular block
        gameManager.update(0.016f)
        
        // Store score after regular block
        val scoreAfterRegularBlock = gameManager.score
        
        // Mark special block as destroyed
        specialBlock.isDestroyed = true
        
        // Update to process the special block
        gameManager.update(0.016f)
        
        // Calculate score difference
        val regularBlockPoints = scoreAfterRegularBlock
        val specialBlockPoints = gameManager.score - scoreAfterRegularBlock
        
        // Verify special block gives more points
        assertTrue(specialBlockPoints > regularBlockPoints)
    }
    
    @Test
    fun `test adding new block row`() {
        // Start the game
        startGame()
        
        // Clear existing blocks
        gameManager.blocks.clear()
        
        // Add a new row of blocks
        addNewBlockRow()
        
        // Verify blocks were added
        assertTrue(gameManager.blocks.isNotEmpty())
        
        // Count blocks in the row
        val blockCount = gameManager.blocks.size
        
        // Add another row
        addNewBlockRow()
        
        // Verify more blocks were added
        assertEquals(blockCount * 2, gameManager.blocks.size)
    }
    
    @Test
    fun `test losing a life`() {
        // Start the game
        startGame()
        
        // Initial lives
        val initialLives = gameManager.lives
        
        // Lose a life
        loseLife()
        
        // Verify lives decreased
        assertEquals(initialLives - 1, gameManager.lives)
        
        // Verify game is still running
        assertTrue(gameManager.gameRunning)
    }
    
    @Test
    fun `test ball waiting for launch`() {
        // Start the game
        startGame()
        
        // Set ball waiting for launch
        gameManager.ballWaitingForLaunch = true
        
        // Store initial ball position
        val initialX = gameManager.ball.x
        val initialY = gameManager.ball.y
        
        // Update the game
        gameManager.update(0.016f)
        
        // Ball position should not change when waiting for launch
        assertEquals(initialX, gameManager.ball.x)
        assertEquals(initialY, gameManager.ball.y)
        
        // Now allow the ball to move
        gameManager.ballWaitingForLaunch = false
        
        // Update the game
        gameManager.update(0.016f)
        
        // Ball position should now change
        assertNotEquals(initialX, gameManager.ball.x)
        assertNotEquals(initialY, gameManager.ball.y)
    }
    
    @Test
    fun `test difficulty progression`() {
        // Start the game
        startGame()
        
        // Use reflection to access private fields
        val blockAddIntervalField = GameManager::class.java.getDeclaredField("blockAddInterval")
        blockAddIntervalField.isAccessible = true
        
        val currentDifficultyLevelField = GameManager::class.java.getDeclaredField("currentDifficultyLevel")
        currentDifficultyLevelField.isAccessible = true
        
        val gameStartTimeField = GameManager::class.java.getDeclaredField("gameStartTime")
        gameStartTimeField.isAccessible = true
        
        // Get initial values
        val initialBlockAddInterval = blockAddIntervalField.getLong(gameManager)
        val initialDifficultyLevel = currentDifficultyLevelField.getInt(gameManager)
        
        // Set game start time to 30 seconds ago to trigger difficulty increase
        val currentTime = System.currentTimeMillis()
        gameStartTimeField.setLong(gameManager, currentTime - 30000)
        
        // Update difficulty
        val updateDifficultyMethod = GameManager::class.java.getDeclaredMethod("updateDifficulty", Long::class.java)
        updateDifficultyMethod.isAccessible = true
        updateDifficultyMethod.invoke(gameManager, currentTime)
        
        // Get updated values
        val updatedBlockAddInterval = blockAddIntervalField.getLong(gameManager)
        val updatedDifficultyLevel = currentDifficultyLevelField.getInt(gameManager)
        
        // Verify difficulty increased
        assertTrue(updatedDifficultyLevel > initialDifficultyLevel)
        
        // Verify block add interval decreased (game gets harder)
        assertTrue(updatedBlockAddInterval < initialBlockAddInterval)
    }
    
    @Test
    fun `test blocks reaching danger zone`() {
        // Start the game
        startGame()
        
        // Clear existing blocks
        gameManager.blocks.clear()
        
        // Add a block in the danger zone
        val dangerZoneField = GameManager::class.java.getDeclaredField("dangerZone")
        dangerZoneField.isAccessible = true
        val dangerZone = dangerZoneField.getFloat(gameManager)
        
        val dangerBlock = Block(
            RectF(100f, screenHeight - dangerZone + 10f, 150f, screenHeight - dangerZone + 50f),
            0
        )
        gameManager.blocks.add(dangerBlock)
        
        // Check blocks position
        checkBlocksPositionMethod.invoke(gameManager)
        
        // Verify game is over
        assertTrue(gameManager.gameOver)
        assertEquals(0, gameManager.lives)
    }
}