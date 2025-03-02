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
import java.lang.reflect.Method

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class CollisionTest {

    @Mock
    private lateinit var mockContext: Context
    
    private lateinit var gameView: GameView
    
    // Methods accessed via reflection since they're private
    private lateinit var lineIntersectsRectMethod: Method
    private lateinit var lineIntersectsLineMethod: Method
    private lateinit var isBlockInSpecialRangeMethod: Method
    private lateinit var isAdjacentMethod: Method
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Mock necessary context methods
        `when`(mockContext.getSystemService(Context.AUDIO_SERVICE)).thenReturn(null)
        
        // Create a GameView instance
        gameView = GameView(mockContext)
        
        // Get private methods via reflection
        lineIntersectsRectMethod = GameView::class.java.getDeclaredMethod(
            "lineIntersectsRect", 
            Float::class.java, Float::class.java, Float::class.java, Float::class.java, RectF::class.java
        ).apply { isAccessible = true }
        
        lineIntersectsLineMethod = GameView::class.java.getDeclaredMethod(
            "lineIntersectsLine", 
            Float::class.java, Float::class.java, Float::class.java, Float::class.java,
            Float::class.java, Float::class.java, Float::class.java, Float::class.java
        ).apply { isAccessible = true }
        
        isBlockInSpecialRangeMethod = GameView::class.java.getDeclaredMethod(
            "isBlockInSpecialRange",
            GameView.Block::class.java, GameView.Block::class.java
        ).apply { isAccessible = true }
        
        isAdjacentMethod = GameView::class.java.getDeclaredMethod(
            "isAdjacent",
            GameView.Block::class.java, GameView.Block::class.java
        ).apply { isAccessible = true }
    }
    
    @Test
    fun `test line intersects rectangle - line passes through`() {
        val rect = RectF(100f, 100f, 200f, 200f)
        val result = lineIntersectsRectMethod.invoke(
            gameView, 50f, 50f, 250f, 250f, rect
        ) as Boolean
        
        assertTrue(result)
    }
    
    @Test
    fun `test line intersects rectangle - line doesn't intersect`() {
        val rect = RectF(100f, 100f, 200f, 200f)
        val result = lineIntersectsRectMethod.invoke(
            gameView, 50f, 50f, 75f, 75f, rect
        ) as Boolean
        
        assertFalse(result)
    }
    
    @Test
    fun `test line intersects rectangle - endpoint inside rectangle`() {
        val rect = RectF(100f, 100f, 200f, 200f)
        val result = lineIntersectsRectMethod.invoke(
            gameView, 50f, 50f, 150f, 150f, rect
        ) as Boolean
        
        assertTrue(result)
    }
    
    @Test
    fun `test line intersects line - lines intersect`() {
        val result = lineIntersectsLineMethod.invoke(
            gameView, 
            0f, 0f, 100f, 100f,  // Line 1: (0,0) to (100,100)
            0f, 100f, 100f, 0f    // Line 2: (0,100) to (100,0)
        ) as Boolean
        
        assertTrue(result)
    }
    
    @Test
    fun `test line intersects line - lines don't intersect`() {
        val result = lineIntersectsLineMethod.invoke(
            gameView, 
            0f, 0f, 50f, 50f,     // Line 1: (0,0) to (50,50)
            60f, 60f, 100f, 100f   // Line 2: (60,60) to (100,100)
        ) as Boolean
        
        assertFalse(result)
    }
    
    @Test
    fun `test block in special range - blocks within range`() {
        val block1 = GameView.Block(RectF(100f, 100f, 150f, 150f), 0, true)
        val block2 = GameView.Block(RectF(200f, 200f, 250f, 250f), 0, false)
        
        val result = isBlockInSpecialRangeMethod.invoke(gameView, block1, block2) as Boolean
        
        assertTrue(result)
    }
    
    @Test
    fun `test block in special range - blocks out of range`() {
        val block1 = GameView.Block(RectF(100f, 100f, 150f, 150f), 0, true)
        val block2 = GameView.Block(RectF(500f, 500f, 550f, 550f), 0, false)
        
        val result = isBlockInSpecialRangeMethod.invoke(gameView, block1, block2) as Boolean
        
        assertFalse(result)
    }
    
    @Test
    fun `test blocks are adjacent - blocks are adjacent`() {
        val block1 = GameView.Block(RectF(100f, 100f, 150f, 150f), 0)
        val block2 = GameView.Block(RectF(150f, 100f, 200f, 150f), 0)
        
        val result = isAdjacentMethod.invoke(gameView, block1, block2) as Boolean
        
        assertTrue(result)
    }
    
    @Test
    fun `test blocks are adjacent - blocks are not adjacent`() {
        val block1 = GameView.Block(RectF(100f, 100f, 150f, 150f), 0)
        val block2 = GameView.Block(RectF(300f, 300f, 350f, 350f), 0)
        
        val result = isAdjacentMethod.invoke(gameView, block1, block2) as Boolean
        
        assertFalse(result)
    }
} 