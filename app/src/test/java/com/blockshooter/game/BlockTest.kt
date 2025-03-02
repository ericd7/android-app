package com.blockshooter.game

import android.graphics.Color
import android.graphics.RectF
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class BlockTest {

    private lateinit var testRect: RectF
    private val testColor = Color.RED
    
    @Before
    fun setUp() {
        testRect = RectF(10f, 20f, 110f, 70f)
    }
    
    @Test
    fun `test block creation with default values`() {
        val block = GameView.Block(testRect, testColor)
        
        assertEquals(testRect, block.rect)
        assertEquals(testColor, block.color)
        assertFalse(block.isSpecial)
    }
    
    @Test
    fun `test block creation with special flag`() {
        val block = GameView.Block(testRect, testColor, true)
        
        assertEquals(testRect, block.rect)
        assertEquals(testColor, block.color)
        assertTrue(block.isSpecial)
    }
    
    @Test
    fun `test block dimensions`() {
        val block = GameView.Block(testRect, testColor)
        
        assertEquals(100f, block.rect.width())
        assertEquals(50f, block.rect.height())
    }
    
    @Test
    fun `test block position`() {
        val block = GameView.Block(testRect, testColor)
        
        assertEquals(10f, block.rect.left)
        assertEquals(20f, block.rect.top)
        assertEquals(110f, block.rect.right)
        assertEquals(70f, block.rect.bottom)
    }
} 