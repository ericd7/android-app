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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class BallTest {

    @Mock
    private lateinit var mockContext: Context
    
    private lateinit var gameView: GameView
    private lateinit var ball: GameView.Ball
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Mock necessary context methods
        `when`(mockContext.getSystemService(Context.AUDIO_SERVICE)).thenReturn(null)
        
        // Create a GameView instance to access the Ball inner class
        gameView = GameView(mockContext)
        
        // Create a Ball instance for testing
        ball = gameView.Ball(100f, 200f, 15f, 10f, -10f)
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
        ball.update()
        
        assertEquals(110f, ball.x)
        assertEquals(190f, ball.y)
    }
    
    @Test
    fun `test ball bounds calculation`() {
        val bounds = ball.getBounds()
        
        assertEquals(85f, bounds.left)
        assertEquals(185f, bounds.top)
        assertEquals(115f, bounds.right)
        assertEquals(215f, bounds.bottom)
    }
    
    @Test
    fun `test ball velocity changes`() {
        ball.velocityX = -5f
        ball.velocityY = 15f
        
        assertEquals(-5f, ball.velocityX)
        assertEquals(15f, ball.velocityY)
        
        ball.update()
        
        assertEquals(-5f + 100f, ball.x)
        assertEquals(15f + 200f, ball.y)
    }
} 