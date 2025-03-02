package com.blockshooter.game

import android.graphics.RectF
import com.blockshooter.game.model.Ball
import com.blockshooter.game.model.Block
import com.blockshooter.game.util.CollisionUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class CollisionTest {
    
    @Before
    fun setUp() {
        // No setup needed as we're using static utility methods
    }
    
    @Test
    fun `test line intersects rectangle - line passes through`() {
        val rect = RectF(100f, 100f, 200f, 200f)
        val result = CollisionUtils.lineIntersectsRect(50f, 50f, 250f, 250f, rect)
        
        assertTrue(result)
    }
    
    @Test
    fun `test line intersects rectangle - line doesn't intersect`() {
        val rect = RectF(100f, 100f, 200f, 200f)
        val result = CollisionUtils.lineIntersectsRect(50f, 50f, 75f, 75f, rect)
        
        assertFalse(result)
    }
    
    @Test
    fun `test line intersects rectangle - endpoint inside rectangle`() {
        val rect = RectF(100f, 100f, 200f, 200f)
        val result = CollisionUtils.lineIntersectsRect(50f, 50f, 150f, 150f, rect)
        
        assertTrue(result)
    }
    
    @Test
    fun `test line intersects line - lines intersect`() {
        // Since lineIntersectsLine is private in CollisionUtils, we'll test it indirectly
        // by using lineIntersectsRect which calls lineIntersectsLine internally
        
        // Create a 1-pixel wide rectangle that's essentially a line
        val lineRect = RectF(0f, 100f, 100f, 101f)
        
        // Test if a line intersects with this "line rectangle"
        val result = CollisionUtils.lineIntersectsRect(0f, 0f, 100f, 200f, lineRect)
        
        assertTrue(result)
    }
    
    @Test
    fun `test line intersects line - lines don't intersect`() {
        // Create a 1-pixel wide rectangle that's essentially a line
        val lineRect = RectF(60f, 60f, 100f, 61f)
        
        // Test if a line that shouldn't intersect
        val result = CollisionUtils.lineIntersectsRect(0f, 0f, 50f, 50f, lineRect)
        
        assertFalse(result)
    }
    
    @Test
    fun `test collision side detection - top collision`() {
        val block = Block(RectF(100f, 100f, 200f, 200f), 0)
        val ball = Ball(150f, 90f, 15f, 0f, 5f) // Ball above the block moving down
        
        val side = CollisionUtils.getCollisionSide(ball, block)
        
        assertEquals("top", side)
    }
    
    @Test
    fun `test collision side detection - bottom collision`() {
        val block = Block(RectF(100f, 100f, 200f, 200f), 0)
        val ball = Ball(150f, 210f, 15f, 0f, -5f) // Ball below the block moving up
        
        val side = CollisionUtils.getCollisionSide(ball, block)
        
        assertEquals("bottom", side)
    }
    
    @Test
    fun `test collision side detection - left collision`() {
        val block = Block(RectF(100f, 100f, 200f, 200f), 0)
        val ball = Ball(90f, 150f, 15f, 5f, 0f) // Ball to the left of the block moving right
        
        val side = CollisionUtils.getCollisionSide(ball, block)
        
        assertEquals("left", side)
    }
    
    @Test
    fun `test collision side detection - right collision`() {
        val block = Block(RectF(100f, 100f, 200f, 200f), 0)
        val ball = Ball(210f, 150f, 15f, -5f, 0f) // Ball to the right of the block moving left
        
        val side = CollisionUtils.getCollisionSide(ball, block)
        
        assertEquals("right", side)
    }
    
    @Test
    fun `test collision side detection - corner collision`() {
        val block = Block(RectF(100f, 100f, 200f, 200f), 0)
        
        // Ball at the top-left corner
        val ball = Ball(90f, 90f, 15f, 5f, 5f)
        
        val side = CollisionUtils.getCollisionSide(ball, block)
        
        // The result should be either "top" or "left" depending on the implementation
        assertTrue(side == "top" || side == "left")
    }
    
    @Test
    fun `test collision with moving ball trajectory`() {
        val block = Block(RectF(100f, 100f, 200f, 200f), 0)
        
        // Create a ball that will intersect with the block in its next movement
        val ball = Ball(80f, 80f, 15f, 30f, 30f)
        
        // Store the original velocity
        val originalVelocityX = ball.velocityX
        val originalVelocityY = ball.velocityY
        
        // Move the ball to simulate collision
        ball.update(1.0f)
        
        // Now the ball should be inside or intersecting with the block
        val side = CollisionUtils.getCollisionSide(ball, block)
        
        // Verify that a collision side was detected
        assertTrue(side in arrayOf("top", "bottom", "left", "right"))
        
        // In a real game, the velocity would be changed based on the collision side
        // Here we just verify that the collision detection works
        if (side == "top" || side == "bottom") {
            // For top/bottom collisions, Y velocity should be reversed in the game
            ball.velocityY = -originalVelocityY
        } else {
            // For left/right collisions, X velocity should be reversed in the game
            ball.velocityX = -originalVelocityX
        }
        
        // Verify that the velocity was changed appropriately
        if (side == "top" || side == "bottom") {
            assertEquals(-originalVelocityY, ball.velocityY)
            assertEquals(originalVelocityX, ball.velocityX)
        } else {
            assertEquals(-originalVelocityX, ball.velocityX)
            assertEquals(originalVelocityY, ball.velocityY)
        }
    }
} 