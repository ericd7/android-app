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
    
    @Test
    fun `test block with rainbow color`() {
        // Test with various rainbow colors
        val rainbowColors = arrayOf(
            Color.rgb(255, 0, 0),      // Red
            Color.rgb(255, 127, 0),    // Orange
            Color.rgb(255, 255, 0),    // Yellow
            Color.rgb(0, 255, 0),      // Green
            Color.rgb(0, 0, 255),      // Blue
            Color.rgb(75, 0, 130),     // Indigo
            Color.rgb(148, 0, 211)     // Violet
        )
        
        for (color in rainbowColors) {
            val block = GameView.Block(testRect, color)
            assertEquals(color, block.color)
        }
    }
} 