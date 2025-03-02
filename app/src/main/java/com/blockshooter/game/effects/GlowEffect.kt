package com.blockshooter.game.effects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * Manages glow effects for special blocks.
 */
class GlowEffect {
    // Glow effect properties
    private var pulseValue = 0f
    private var pulseDirection = 1f
    private val pulseSpeed = 2.0f // Increased for deltaTime (was 0.05f)
    private val pulseMin = 0.6f
    private val pulseMax = 1.0f
    
    /**
     * Updates the glow pulse effect.
     * @param deltaTime The time elapsed since the last update in seconds
     */
    fun update(deltaTime: Float = 0.016f) { // Default to ~60fps if not provided
        pulseValue += pulseDirection * pulseSpeed * deltaTime
        
        if (pulseValue >= pulseMax) {
            pulseValue = pulseMax
            pulseDirection = -1f
        } else if (pulseValue <= pulseMin) {
            pulseValue = pulseMin
            pulseDirection = 1f
        }
    }
    
    /**
     * Draws a glow effect around a block.
     * 
     * @param canvas The canvas to draw on
     * @param paint The paint to use for drawing
     * @param rect The rectangle to draw the glow around
     * @param cornerRadius The corner radius of the rectangle
     * @param color The color of the glow
     */
    fun drawGlow(canvas: Canvas, paint: Paint, rect: RectF, cornerRadius: Float, color: Int) {
        // Calculate glow size based on pulse value
        val glowSize = 8f * pulseValue
        
        // Create a rectangle for the glow
        val glowRect = RectF(
            rect.left - glowSize,
            rect.top - glowSize,
            rect.right + glowSize,
            rect.bottom + glowSize
        )
        
        // Calculate alpha based on pulse value
        val glowAlpha = (100 * pulseValue).toInt()
        
        // Set the paint color with the calculated alpha
        paint.color = Color.argb(
            glowAlpha,
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
        
        // Draw the glow
        canvas.drawRoundRect(glowRect, cornerRadius + glowSize, cornerRadius + glowSize, paint)
    }
    
    /**
     * Gets the current pulse value.
     * 
     * @return The current pulse value
     */
    fun getPulseValue(): Float {
        return pulseValue
    }
    
    /**
     * Sets the pulse value.
     * 
     * @param value The new pulse value
     */
    fun setPulseValue(value: Float) {
        pulseValue = value.coerceIn(pulseMin, pulseMax)
    }
    
    /**
     * Gets the pulse speed.
     * 
     * @return The pulse speed
     */
    fun getPulseSpeed(): Float {
        return pulseSpeed
    }
    
    /**
     * Gets the minimum pulse value.
     * 
     * @return The minimum pulse value
     */
    fun getPulseMin(): Float {
        return pulseMin
    }
    
    /**
     * Gets the maximum pulse value.
     * 
     * @return The maximum pulse value
     */
    fun getPulseMax(): Float {
        return pulseMax
    }
} 