package com.blockshooter.game

import android.content.Context
import android.graphics.RectF
import com.blockshooter.game.model.Ball
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.sqrt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class BallTest {

    @Mock
    private lateinit var mockContext: Context
    
    private lateinit var ball: Ball
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Create a Ball instance for testing
        ball = Ball(100f, 200f, 15f, 10f, -10f)
    }
    
    @Test
    fun `test ball initialization`() {
        assertEquals(100f, ball.x)
        assertEquals(200f, ball.y)
        assertEquals(15f, ball.radius)
        assertEquals(10f, ball.velocityX)
        assertEquals(-10f, ball.velocityY)
    }
    
    @Test
    fun `test ball update position`() {
        // Use a deltaTime of 1.0f for simple testing
        ball.update(1.0f)
        
        assertEquals(110f, ball.x)
        assertEquals(190f, ball.y)
    }
    
    @Test
    fun `test ball bounds calculation`() {
        // Create a rectangle to test intersection
        val rect = RectF(85f, 185f, 115f, 215f)
        
        // The ball should intersect with this rectangle
        assertTrue(ball.intersects(rect))
        
        // Create a rectangle that doesn't intersect
        val noIntersectRect = RectF(200f, 300f, 250f, 350f)
        
        // The ball should not intersect with this rectangle
        assertFalse(ball.intersects(noIntersectRect))
    }
    
    @Test
    fun `test ball velocity changes`() {
        ball.velocityX = -5f
        ball.velocityY = 15f
        
        assertEquals(-5f, ball.velocityX)
        assertEquals(15f, ball.velocityY)
        
        // Use a deltaTime of 1.0f for simple testing
        ball.update(1.0f)
        
        assertEquals(-5f + 100f, ball.x)
        assertEquals(15f + 200f, ball.y)
    }
    
    @Test
    fun `test ball speed limit`() {
        // Set a very high velocity that should be limited
        ball.velocityX = 1000f
        ball.velocityY = 1000f
        
        // Calculate the expected speed (1200f is the max speed defined in Ball.kt)
        val initialSpeed = sqrt(ball.velocityX * ball.velocityX + ball.velocityY * ball.velocityY)
        
        // Update the ball to trigger speed limiting
        ball.update(1.0f)
        
        // Calculate the actual speed after update
        val actualSpeed = sqrt(ball.velocityX * ball.velocityX + ball.velocityY * ball.velocityY)
        
        // The actual speed should be less than or equal to 1200f (max speed)
        assertTrue(actualSpeed <= 1200f)
        
        // The actual speed should be less than the initial speed
        assertTrue(actualSpeed < initialSpeed)
    }
    
    @Test
    fun `test ball copy`() {
        val copiedBall = ball.copy()
        
        // Verify the copied ball has the same properties
        assertEquals(ball.x, copiedBall.x)
        assertEquals(ball.y, copiedBall.y)
        assertEquals(ball.radius, copiedBall.radius)
        assertEquals(ball.velocityX, copiedBall.velocityX)
        assertEquals(ball.velocityY, copiedBall.velocityY)
        
        // Verify that changing the copy doesn't affect the original
        copiedBall.x = 300f
        copiedBall.velocityX = 20f
        
        assertEquals(100f, ball.x)
        assertEquals(10f, ball.velocityX)
    }
} 