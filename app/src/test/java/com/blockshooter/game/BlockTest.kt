package com.blockshooter.game

import android.graphics.Color
import android.graphics.RectF
import com.blockshooter.game.model.Block
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
        val block = Block(testRect, testColor)
        
        assertEquals(testRect, block.rect)
        assertEquals(testColor, block.color)
        assertFalse(block.isSpecial)
        assertEquals(1, block.health)
        assertFalse(block.isDestroyed)
        assertEquals(0f, block.destroyProgress)
    }
    
    @Test
    fun `test block creation with special flag`() {
        val block = Block(testRect, testColor, true)
        
        assertEquals(testRect, block.rect)
        assertEquals(testColor, block.color)
        assertTrue(block.isSpecial)
    }
    
    @Test
    fun `test block dimensions`() {
        val block = Block(testRect, testColor)
        
        assertEquals(100f, block.rect.width())
        assertEquals(50f, block.rect.height())
    }
    
    @Test
    fun `test block position`() {
        val block = Block(testRect, testColor)
        
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
            val block = Block(testRect, color)
            assertEquals(color, block.color)
        }
    }
    
    @Test
    fun `test block damage and destruction`() {
        val block = Block(testRect, testColor, health = 2)
        
        assertEquals(2, block.health)
        assertFalse(block.isDestroyed)
        
        // First damage
        val firstDamageResult = block.damage()
        assertEquals(1, block.health)
        assertFalse(firstDamageResult)
        
        // Second damage should destroy the block
        val secondDamageResult = block.damage()
        assertEquals(0, block.health)
        assertTrue(secondDamageResult)
    }
    
    @Test
    fun `test block contains point`() {
        val block = Block(testRect, testColor)
        
        // Point inside the block
        assertTrue(block.contains(50f, 40f))
        
        // Points outside the block
        assertFalse(block.contains(5f, 40f))
        assertFalse(block.contains(50f, 10f))
        assertFalse(block.contains(120f, 40f))
        assertFalse(block.contains(50f, 80f))
    }
    
    @Test
    fun `test block destroy animation progress`() {
        val block = Block(testRect, testColor)
        
        // Initially, destroy progress should be 0
        assertEquals(0f, block.destroyProgress)
        
        // Set destroy progress
        block.destroyProgress = 0.5f
        assertEquals(0.5f, block.destroyProgress)
        
        // Set block as destroyed
        block.isDestroyed = true
        assertTrue(block.isDestroyed)
        
        // Set destroy progress to complete
        block.destroyProgress = 1.0f
        assertEquals(1.0f, block.destroyProgress)
    }
    
    @Test
    fun `test block copy`() {
        val block = Block(testRect, testColor, true, 2)
        block.isDestroyed = true
        block.destroyProgress = 0.75f
        
        val copiedBlock = block.copy()
        
        // Verify the copied block has the same properties
        assertEquals(block.rect.left, copiedBlock.rect.left)
        assertEquals(block.rect.top, copiedBlock.rect.top)
        assertEquals(block.rect.right, copiedBlock.rect.right)
        assertEquals(block.rect.bottom, copiedBlock.rect.bottom)
        assertEquals(block.color, copiedBlock.color)
        assertEquals(block.isSpecial, copiedBlock.isSpecial)
        assertEquals(block.health, copiedBlock.health)
        assertEquals(block.isDestroyed, copiedBlock.isDestroyed)
        assertEquals(block.destroyProgress, copiedBlock.destroyProgress)
        
        // Verify that the copied block has a different RectF instance
        assertNotSame(block.rect, copiedBlock.rect)
    }
} 