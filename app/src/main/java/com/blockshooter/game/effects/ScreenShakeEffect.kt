package com.blockshooter.game.effects

import kotlin.random.Random

/**
 * Manages screen shake effects in the game.
 */
class ScreenShakeEffect {
    // Screen shake properties
    private var shakeDuration = 0f // Duration in seconds
    private var shakeIntensity = 0f
    var offsetX = 0f
        private set
    var offsetY = 0f
        private set
    
    // Constants
    private val defaultDuration = 0.16f // About 10 frames at 60fps
    
    /**
     * Updates the screen shake effect.
     * @param deltaTime The time elapsed since the last update in seconds
     */
    fun update(deltaTime: Float = 0.016f) { // Default to ~60fps if not provided
        if (shakeDuration > 0) {
            shakeDuration -= deltaTime
            
            // Calculate random offset based on current intensity
            val intensityFactor = (shakeDuration / defaultDuration).coerceIn(0f, 1f)
            val intensity = shakeIntensity * intensityFactor
            offsetX = (Random.nextFloat() * 2 - 1) * intensity
            offsetY = (Random.nextFloat() * 2 - 1) * intensity
        } else {
            shakeDuration = 0f
            offsetX = 0f
            offsetY = 0f
        }
    }
    
    /**
     * Starts a screen shake effect.
     * 
     * @param intensity The intensity of the shake
     * @param durationFrames The duration of the shake in frames (at 60fps)
     */
    fun startScreenShake(intensity: Float = 15f, durationFrames: Int = 10) {
        shakeIntensity = intensity
        shakeDuration = durationFrames / 60f // Convert frames to seconds (assuming 60fps)
    }
    
    /**
     * Checks if the screen is currently shaking.
     * 
     * @return True if the screen is shaking, false otherwise
     */
    fun isShaking(): Boolean {
        return shakeDuration > 0
    }
    
    /**
     * Gets the current shake intensity.
     * 
     * @return The current shake intensity
     */
    fun getShakeIntensity(): Float {
        return if (shakeDuration > 0) {
            shakeIntensity * (shakeDuration / defaultDuration).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    
    /**
     * Gets the remaining shake time.
     * 
     * @return The remaining shake time in seconds
     */
    fun getRemainingShakeDuration(): Float {
        return shakeDuration
    }
    
    /**
     * Stops the screen shake effect immediately.
     */
    fun stopShake() {
        shakeDuration = 0f
        offsetX = 0f
        offsetY = 0f
    }
} 