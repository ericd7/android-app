package com.blockshooter.game.model

import kotlin.math.abs

/**
 * Represents a particle for visual effects in the game.
 * 
 * @property x The x-coordinate of the particle
 * @property y The y-coordinate of the particle
 * @property velocityX The horizontal velocity of the particle
 * @property velocityY The vertical velocity of the particle
 * @property color The color of the particle
 * @property maxLife The maximum life of the particle in frames
 */
class Particle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    val color: Int,
    val maxLife: Int = 30
) {
    // Current life of the particle
    var life: Int = maxLife
    
    // Gravity effect on particles
    private val gravity = 25f
    
    /**
     * Updates the particle's position and reduces its life.
     * @param deltaTime The time elapsed since the last update in seconds
     */
    fun update(deltaTime: Float) {
        // Apply gravity to vertical velocity
        velocityY += gravity * deltaTime
        
        // Apply air resistance
        velocityX *= (1 - 0.05f * deltaTime)
        velocityY *= (1 - 0.02f * deltaTime)
        
        // Update position
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        
        // Reduce life
        life--
        
        // If particle is almost dead and moving very slowly, make it fade faster
        if (life < 5 && abs(velocityX) < 0.1f && abs(velocityY) < 0.1f) {
            life = 0
        }
    }
    
    /**
     * Calculates the current alpha value based on remaining life.
     * @return Alpha value between 0 and 255
     */
    fun getAlpha(): Int {
        return ((life.toFloat() / maxLife) * 255).toInt()
    }
    
    /**
     * Calculates the current size based on remaining life.
     * @param baseSize The base size of the particle
     * @return Current size of the particle
     */
    fun getCurrentSize(baseSize: Float): Float {
        return baseSize * (life.toFloat() / maxLife)
    }
} 