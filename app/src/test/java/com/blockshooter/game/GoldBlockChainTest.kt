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
class GoldBlockChainTest {

    @Mock
    private lateinit var mockContext: Context
    
    private lateinit var gameView: GameView
    
    // Fields accessed via reflection
    private lateinit var blocksField: Field
    private lateinit var scoreField: Field
    
    // Methods accessed via reflection
    private lateinit var processSpecialBlockChainMethod: Method
    private lateinit var isBlockInSpecialRangeMethod: Method
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Mock necessary context methods
        `when`(mockContext.getSystemService(Context.AUDIO_SERVICE)).thenReturn(null)
        
        // Create a GameView instance
        gameView = GameView(mockContext)
        
        // Get private fields via reflection
        blocksField = GameView::class.java.getDeclaredField("blocks").apply { isAccessible = true }
        scoreField = GameView::class.java.getDeclaredField("score").apply { isAccessible = true }
        
        // Get methods via reflection
        processSpecialBlockChainMethod = GameView::class.java.getDeclaredMethod(
            "processSpecialBlockChain",
            GameView.Block::class.java
        ).apply { isAccessible = true }
        
        isBlockInSpecialRangeMethod = GameView::class.java.getDeclaredMethod(
            "isBlockInSpecialRange",
            GameView.Block::class.java, GameView.Block::class.java
        ).apply { isAccessible = true }
    }
    
    @Test
    fun `test single special block destruction`() {
        // Create test blocks
        val blocks = mutableListOf<GameView.Block>()
        
        // Special block
        val specialBlock = GameView.Block(RectF(100f, 100f, 150f, 150f), 0, true)
        
        // Regular blocks in range
        val regularBlock1 = GameView.Block(RectF(150f, 100f, 200f, 150f), 0, false)
        val regularBlock2 = GameView.Block(RectF(100f, 150f, 150f, 200f), 0, false)
        
        // Block out of range
        val outOfRangeBlock = GameView.Block(RectF(300f, 300f, 350f, 350f), 0, false)
        
        // Add blocks to the list
        blocks.add(specialBlock)
        blocks.add(regularBlock1)
        blocks.add(regularBlock2)
        blocks.add(outOfRangeBlock)
        
        // Set blocks in the GameView
        blocksField.set(gameView, blocks)
        
        // Reset score
        scoreField.set(gameView, 0)
        
        // Process the special block chain
        processSpecialBlockChainMethod.invoke(gameView, specialBlock)
        
        // Get updated blocks list
        val updatedBlocks = blocksField.get(gameView) as MutableList<*>
        
        // Check that the special block and blocks in range were removed
        assertEquals(1, updatedBlocks.size)
        assertTrue(outOfRangeBlock in updatedBlocks)
        
        // Check score: 10 points per block (3 blocks) + 20 bonus for special block
        assertEquals(50, scoreField.get(gameView))
    }
    
    @Test
    fun `test chain reaction with multiple special blocks`() {
        // Create test blocks
        val blocks = mutableListOf<GameView.Block>()
        
        // First special block
        val specialBlock1 = GameView.Block(RectF(100f, 100f, 150f, 150f), 0, true)
        
        // Second special block in range of the first
        val specialBlock2 = GameView.Block(RectF(200f, 200f, 250f, 250f), 0, true)
        
        // Regular blocks in range of the second special block
        val regularBlock1 = GameView.Block(RectF(250f, 200f, 300f, 250f), 0, false)
        val regularBlock2 = GameView.Block(RectF(200f, 250f, 250f, 300f), 0, false)
        
        // Block out of range of both special blocks
        val outOfRangeBlock = GameView.Block(RectF(500f, 500f, 550f, 550f), 0, false)
        
        // Add blocks to the list
        blocks.add(specialBlock1)
        blocks.add(specialBlock2)
        blocks.add(regularBlock1)
        blocks.add(regularBlock2)
        blocks.add(outOfRangeBlock)
        
        // Set blocks in the GameView
        blocksField.set(gameView, blocks)
        
        // Reset score
        scoreField.set(gameView, 0)
        
        // Verify that the second special block is in range of the first
        val inRange = isBlockInSpecialRangeMethod.invoke(
            gameView, specialBlock1, specialBlock2
        ) as Boolean
        assertTrue(inRange)
        
        // Process the special block chain
        processSpecialBlockChainMethod.invoke(gameView, specialBlock1)
        
        // Get updated blocks list
        val updatedBlocks = blocksField.get(gameView) as MutableList<*>
        
        // Check that all blocks except the out-of-range one were removed
        assertEquals(1, updatedBlocks.size)
        assertTrue(outOfRangeBlock in updatedBlocks)
        
        // Check score: 10 points per block (4 blocks) + 20 bonus for each special block (2 blocks)
        assertEquals(80, scoreField.get(gameView))
    }
} 