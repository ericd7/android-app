package com.blockshooter.game.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.blockshooter.game.R
import kotlin.math.sin

/**
 * A custom view that draws an animated paddle at the bottom of the main menu screen.
 */
class MenuPaddleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paddleRect = RectF()
    private val shadowRect = RectF()
    
    // Paddle properties
    private val paddleWidth = 200f
    private val paddleHeight = 30f
    private val paddleCornerRadius = 15f
    private val paddleShadowOffset = 4f
    private val paddleShadowColor = Color.argb(100, 0, 0, 0) // Semi-transparent black for shadows
    
    // Animation properties
    private var animationTime = 0f
    private val animationSpeed = 0.02f // Controls the speed of the paddle
    private val animationAmplitude = 0.8f // Controls how much of the screen width the paddle traverses
    
    // Gradient colors for the paddle
    private var paddleGradient: LinearGradient? = null
    private val primaryColor = ContextCompat.getColor(context, R.color.game_primary)
    private val accentColor = ContextCompat.getColor(context, R.color.game_accent)
    
    init {
        // Start the animation
        postInvalidateOnAnimation()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Create gradient for paddle
        paddleGradient = LinearGradient(
            0f, 0f, paddleWidth, 0f,
            primaryColor, accentColor,
            Shader.TileMode.CLAMP
        )
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Calculate paddle position based on sine wave animation
        val screenWidth = width.toFloat()
        val centerX = screenWidth / 2
        val amplitude = (screenWidth - paddleWidth) * animationAmplitude / 2
        
        // Calculate x position using sine wave
        val xOffset = sin(animationTime) * amplitude
        val paddleX = centerX - (paddleWidth / 2) + xOffset
        
        // Position paddle at the bottom of the view with some margin
        val paddleY = height - paddleHeight - 50f // 50px margin from bottom
        
        // Update paddle rectangle
        paddleRect.set(
            paddleX,
            paddleY,
            paddleX + paddleWidth,
            paddleY + paddleHeight
        )
        
        // Draw paddle shadow
        shadowRect.set(
            paddleRect.left + paddleShadowOffset,
            paddleRect.top + paddleShadowOffset,
            paddleRect.right + paddleShadowOffset,
            paddleRect.bottom + paddleShadowOffset
        )
        paint.color = paddleShadowColor
        canvas.drawRoundRect(shadowRect, paddleCornerRadius, paddleCornerRadius, paint)
        
        // Draw paddle with gradient
        paint.shader = paddleGradient
        canvas.drawRoundRect(paddleRect, paddleCornerRadius, paddleCornerRadius, paint)
        paint.shader = null
        
        // Add highlight effect on top of paddle
        paint.color = Color.argb(80, 255, 255, 255)
        val highlightRect = RectF(
            paddleRect.left,
            paddleRect.top,
            paddleRect.right,
            paddleRect.top + paddleHeight / 3
        )
        canvas.drawRoundRect(highlightRect, paddleCornerRadius, paddleCornerRadius, paint)
        
        // Update animation time
        animationTime += animationSpeed
        
        // Continue animation
        postInvalidateOnAnimation()
    }
} 