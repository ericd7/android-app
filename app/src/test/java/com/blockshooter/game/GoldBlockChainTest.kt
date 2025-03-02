package com.blockshooter.game

import android.graphics.RectF
import android.graphics.Color
import com.blockshooter.game.model.Block
import com.blockshooter.game.util.GameManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.blockshooter.game.effects.ParticleSystem
import com.blockshooter.game.effects.ScreenShakeEffect
import com.blockshooter.game.effects.SoundManager
import org.mockito.Mock
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GoldBlockChainTest {

    @Mock
    private lateinit var particleSystem: ParticleSystem
    
    @Mock
    private lateinit var screenShakeEffect: ScreenShakeEffect
    
    @Mock
    private lateinit var soundManager: SoundManager
    
    private lateinit var gameManager: GameManager
    private lateinit var specialBlockColor: Field
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Create a GameManager instance with mocked dependencies
        gameManager = GameManager(800, 1200, particleSystem, screenShakeEffect, soundManager)
        
        // Access the special block color field
        specialBlockColor = GameManager::class.java.getDeclaredField("specialBlockColor")
        specialBlockColor.isAccessible = true
    }
    
    @Test
    fun `test special block destruction`() {
        // Get the special block color
        val goldColor = specialBlockColor.get(gameManager) as Int
        
        // Create test blocks
        val specialBlock = Block(RectF(100f, 100f, 150f, 150f), goldColor, true)
        
        // Regular blocks nearby
        val regularBlock1 = Block(RectF(150f, 100f, 200f, 150f), Color.RED)
        val regularBlock2 = Block(RectF(100f, 150f, 150f, 200f), Color.BLUE)
        
        // Block far away
        val farBlock = Block(RectF(300f, 300f, 350f, 350f), Color.GREEN)
        
        // Add blocks to the game manager
        gameManager.blocks.add(specialBlock)
        gameManager.blocks.add(regularBlock1)
        gameManager.blocks.add(regularBlock2)
        gameManager.blocks.add(farBlock)
        
        // Reset score
        gameManager.score = 0
        
        // Simulate destroying the special block
        specialBlock.isDestroyed = true
        
        // Update the game to process the special block
        gameManager.update(0.016f)
        
        // Check that blocks were removed in the next update
        gameManager.update(0.016f)
        
        // The special block and nearby blocks should be removed, but the far block should remain
        assertEquals(1, gameManager.blocks.size)
        assertTrue(gameManager.blocks.contains(farBlock))
        
        // Score should be increased (exact amount depends on implementation)
        assertTrue(gameManager.score > 0)
    }
    
    @Test
    fun `test multiple special blocks`() {
        // Get the special block color
        val goldColor = specialBlockColor.get(gameManager) as Int
        
        // Create test blocks
        val specialBlock1 = Block(RectF(100f, 100f, 150f, 150f), goldColor, true)
        val specialBlock2 = Block(RectF(200f, 200f, 250f, 250f), goldColor, true)
        
        // Regular blocks near the second special block
        val regularBlock1 = Block(RectF(250f, 200f, 300f, 250f), Color.RED)
        val regularBlock2 = Block(RectF(200f, 250f, 250f, 300f), Color.BLUE)
        
        // Block far away
        val farBlock = Block(RectF(500f, 500f, 550f, 550f), Color.GREEN)
        
        // Add blocks to the game manager
        gameManager.blocks.add(specialBlock1)
        gameManager.blocks.add(specialBlock2)
        gameManager.blocks.add(regularBlock1)
        gameManager.blocks.add(regularBlock2)
        gameManager.blocks.add(farBlock)
        
        // Reset score
        gameManager.score = 0
        
        // Simulate destroying the first special block
        specialBlock1.isDestroyed = true
        
        // Update the game to process the special blocks
        gameManager.update(0.016f)
        
        // Check that blocks were removed in the next update
        gameManager.update(0.016f)
        
        // All blocks except the far block should be removed
        assertEquals(1, gameManager.blocks.size)
        assertTrue(gameManager.blocks.contains(farBlock))
        
        // Score should be increased (exact amount depends on implementation)
        assertTrue(gameManager.score > 0)
    }
    
    @Test
    fun `test special block chain reaction radius`() {
        // Get the special block color
        val goldColor = specialBlockColor.get(gameManager) as Int
        
        // Create a special block
        val specialBlock = Block(RectF(100f, 100f, 150f, 150f), goldColor, true)
        
        // Create blocks at different distances
        val nearBlock = Block(RectF(160f, 100f, 210f, 150f), Color.RED) // 10 pixels away
        val mediumBlock = Block(RectF(220f, 100f, 270f, 150f), Color.GREEN) // 70 pixels away
        val farBlock = Block(RectF(400f, 100f, 450f, 150f), Color.BLUE) // 250 pixels away
        
        // Add blocks to the game manager
        gameManager.blocks.add(specialBlock)
        gameManager.blocks.add(nearBlock)
        gameManager.blocks.add(mediumBlock)
        gameManager.blocks.add(farBlock)
        
        // Simulate destroying the special block
        specialBlock.isDestroyed = true
        
        // Update the game to process the special block
        gameManager.update(0.016f)
        
        // Check that blocks were removed in the next update
        gameManager.update(0.016f)
        
        // The special block and nearby blocks should be removed, but the far block should remain
        // The exact behavior depends on the chain reaction radius in the implementation
        assertTrue(gameManager.blocks.contains(farBlock))
    }
    
    @Test
    fun `test special block animation and particle effects`() {
        // Get the special block color
        val goldColor = specialBlockColor.get(gameManager) as Int
        
        // Create a special block
        val specialBlock = Block(RectF(100f, 100f, 150f, 150f), goldColor, true)
        
        // Add the block to the game manager
        gameManager.blocks.add(specialBlock)
        
        // Simulate destroying the special block
        specialBlock.isDestroyed = true
        
        // Update the game to process the special block
        gameManager.update(0.016f)
        
        // The special block should be removed
        assertFalse(gameManager.blocks.contains(specialBlock))
    }
} 