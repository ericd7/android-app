package com.blockshooter.game

import android.content.Context
import android.graphics.RectF
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Field
import java.lang.reflect.Method

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GameLogicTest {

    @Mock
    private lateinit var mockContext: Context
    
    private lateinit var gameView: GameView
    
    // Fields accessed via reflection
    private lateinit var scoreField: Field
    private lateinit var livesField: Field
    private lateinit var gameRunningField: Field
    private lateinit var blocksField: Field
    private lateinit var totalRowsAddedField: Field
    
    // Methods accessed via reflection
    private lateinit var startGameMethod: Method
    private lateinit var endGameMethod: Method
    private lateinit var createBlocksMethod: Method
    private lateinit var addNewBlockRowMethod: Method
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Mock necessary context methods
        `when`(mockContext.getSystemService(Context.AUDIO_SERVICE)).thenReturn(null)
        
        // Create a GameView instance
        gameView = GameView(mockContext)
        
        // Get private fields via reflection
        scoreField = GameView::class.java.getDeclaredField("score").apply { isAccessible = true }
        livesField = GameView::class.java.getDeclaredField("lives").apply { isAccessible = true }
        gameRunningField = GameView::class.java.getDeclaredField("gameRunning").apply { isAccessible = true }
        blocksField = GameView::class.java.getDeclaredField("blocks").apply { isAccessible = true }
        totalRowsAddedField = GameView::class.java.getDeclaredField("totalRowsAdded").apply { isAccessible = true }
        
        // Get methods via reflection
        startGameMethod = GameView::class.java.getDeclaredMethod("startGame").apply { isAccessible = true }
        endGameMethod = GameView::class.java.getDeclaredMethod("endGame").apply { isAccessible = true }
        createBlocksMethod = GameView::class.java.getDeclaredMethod("createBlocks").apply { isAccessible = true }
        addNewBlockRowMethod = GameView::class.java.getDeclaredMethod("addNewBlockRow").apply { isAccessible = true }
    }
    
    @Test
    fun `test game initialization`() {
        // Check initial game state
        assertEquals(0, scoreField.get(gameView))
        assertEquals(3, livesField.get(gameView))
        assertFalse(gameRunningField.get(gameView) as Boolean)
        
        // Get blocks list
        val blocks = blocksField.get(gameView) as MutableList<*>
        assertTrue(blocks.isEmpty())
    }
    
    @Test
    fun `test start game`() {
        // Start the game
        startGameMethod.invoke(gameView)
        
        // Check game state after starting
        assertTrue(gameRunningField.get(gameView) as Boolean)
        assertEquals(0, scoreField.get(gameView))
        assertEquals(3, livesField.get(gameView))
        assertEquals(0, totalRowsAddedField.get(gameView))
        
        // Check that blocks were created
        val blocks = blocksField.get(gameView) as MutableList<*>
        assertFalse(blocks.isEmpty())
    }
    
    @Test
    fun `test end game`() {
        // Start the game first
        startGameMethod.invoke(gameView)
        assertTrue(gameRunningField.get(gameView) as Boolean)
        
        // End the game
        endGameMethod.invoke(gameView)
        
        // Check game state after ending
        assertFalse(gameRunningField.get(gameView) as Boolean)
    }
    
    @Test
    fun `test create blocks`() {
        // Create blocks
        createBlocksMethod.invoke(gameView)
        
        // Check that blocks were created
        val blocks = blocksField.get(gameView) as MutableList<*>
        assertFalse(blocks.isEmpty())
        
        // Check that totalRowsAdded was set
        val blockRowsField = GameView::class.java.getDeclaredField("blockRows").apply { isAccessible = true }
        val blockRows = blockRowsField.get(gameView) as Int
        assertEquals(blockRows, totalRowsAddedField.get(gameView))
        
        // Verify no special blocks in initial rows
        val anySpecialBlocks = blocks.any { 
            val block = it as GameView.Block
            block.isSpecial
        }
        assertFalse(anySpecialBlocks)
    }
    
    @Test
    fun `test rainbow color pattern in blocks`() {
        // Get access to the rainbowColors field
        val rainbowColorsField = GameView::class.java.getDeclaredField("rainbowColors").apply { isAccessible = true }
        val rainbowColors = rainbowColorsField.get(gameView) as Array<*>
        
        // Create blocks
        createBlocksMethod.invoke(gameView)
        
        // Get blocks
        val blocks = blocksField.get(gameView) as MutableList<GameView.Block>
        
        // Get blockCols and blockRows
        val blockColsField = GameView::class.java.getDeclaredField("blockCols").apply { isAccessible = true }
        val blockCols = blockColsField.get(gameView) as Int
        val blockRowsField = GameView::class.java.getDeclaredField("blockRows").apply { isAccessible = true }
        val blockRows = blockRowsField.get(gameView) as Int
        
        // Verify that blocks in the same row have the same color
        for (row in 0 until blockRows) {
            val expectedColor = rainbowColors[row % rainbowColors.size] as Int
            
            for (col in 0 until blockCols) {
                val blockIndex = row * blockCols + col
                val block = blocks[blockIndex]
                
                // Skip special blocks (though there shouldn't be any in initial rows)
                if (!block.isSpecial) {
                    assertEquals("Block at row $row, col $col has incorrect color", 
                                expectedColor, block.color)
                }
            }
        }
    }
    
    @Test
    fun `test add new block row`() {
        // First create initial blocks
        createBlocksMethod.invoke(gameView)
        
        // Get initial block count
        val blocks = blocksField.get(gameView) as MutableList<*>
        val initialBlockCount = blocks.size
        
        // Get initial totalRowsAdded
        val initialTotalRowsAdded = totalRowsAddedField.get(gameView) as Int
        
        // Add a new row
        addNewBlockRowMethod.invoke(gameView)
        
        // Check that blocks were added
        val newBlockCount = blocks.size
        val blockColsField = GameView::class.java.getDeclaredField("blockCols").apply { isAccessible = true }
        val blockCols = blockColsField.get(gameView) as Int
        
        assertEquals(initialBlockCount + blockCols, newBlockCount)
        
        // Check that totalRowsAdded was incremented
        assertEquals(initialTotalRowsAdded + 1, totalRowsAddedField.get(gameView))
    }
    
    @Test
    fun `test score increases when blocks are removed`() {
        // Start with a clean game
        startGameMethod.invoke(gameView)
        
        // Set initial score
        scoreField.set(gameView, 0)
        
        // Get access to the checkBlockCollisions method
        val checkBlockCollisionsMethod = GameView::class.java.getDeclaredMethod(
            "checkBlockCollisions",
            Float::class.java, Float::class.java, Float::class.java, Float::class.java
        ).apply { isAccessible = true }
        
        // Get access to the ball field
        val ballField = GameView::class.java.getDeclaredField("ball").apply { isAccessible = true }
        
        // Get the blocks
        val blocks = blocksField.get(gameView) as MutableList<GameView.Block>
        
        // Ensure we have blocks
        assertFalse(blocks.isEmpty())
        
        // Get the first block's position
        val firstBlock = blocks[0]
        val blockCenterX = firstBlock.rect.left + (firstBlock.rect.right - firstBlock.rect.left) / 2
        val blockCenterY = firstBlock.rect.top + (firstBlock.rect.bottom - firstBlock.rect.top) / 2
        
        // Set the ball's position to collide with the block
        val ball = ballField.get(gameView) as GameView.Ball
        ball.x = blockCenterX
        ball.y = blockCenterY + 30f // Position below the block
        ball.velocityY = -10f // Moving upward
        
        // Simulate collision detection
        checkBlockCollisionsMethod.invoke(
            gameView,
            ball.x, ball.y, // Start position
            ball.x, ball.y - 10f // End position (moving upward)
        )
        
        // Check that score increased
        val newScore = scoreField.get(gameView) as Int
        assertTrue(newScore > 0)
    }
} 