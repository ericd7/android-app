package com.blockshooter.game.effects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.random.Random

/**
 * Manages particle effects in the game.
 */
class ParticleSystem {
    // Particle settings
    private val particleLifespan = 30 // frames
    private val particleSize = 8f
    private val particleCount = 10 // Reduced from 15 to 10 particles per block
    private val particleMaxSpeed = 600f // Increased for deltaTime
    
    // Particle collection and pool
    private val particles = CopyOnWriteArrayList<Particle>()
    private val particlePool = ArrayList<Particle>(100) // Particle pool to reuse objects
    
    init {
        // Pre-populate the particle pool
        for (i in 0 until 50) {
            particlePool.add(Particle(0f, 0f, 0f, 0f, Color.WHITE, 0))
        }
    }
    
    /**
     * Updates all particles.
     * @param deltaTime The time elapsed since the last update in seconds
     */
    fun update(deltaTime: Float) {
        try {
            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val particle = iterator.next()
                particle.update(deltaTime)
                if (particle.life <= 0) {
                    // Return particle to pool instead of just removing it
                    recycleParticle(particle)
                    iterator.remove()
                }
            }
            
            // Safety check: clear all particles if there are too many
            if (particles.size > 300) { // Reduced from 500 to 300
                recycleAllParticles()
            }
        } catch (e: Exception) {
            // If any exception occurs, clear all particles to prevent issues
            recycleAllParticles()
            e.printStackTrace()
        }
    }
    
    /**
     * Recycles a particle back to the pool
     */
    private fun recycleParticle(particle: Particle) {
        // Only add to pool if it's not too large
        if (particlePool.size < 100) {
            particlePool.add(particle)
        }
    }
    
    /**
     * Recycles all active particles back to the pool
     */
    private fun recycleAllParticles() {
        // Add particles back to pool and clear active list
        for (particle in particles) {
            if (particlePool.size < 100) {
                particlePool.add(particle)
            }
        }
        particles.clear()
    }
    
    /**
     * Gets a particle from the pool or creates a new one
     */
    private fun obtainParticle(x: Float, y: Float, velocityX: Float, velocityY: Float, color: Int, life: Int): Particle {
        // Try to get from pool first
        if (particlePool.isNotEmpty()) {
            val particle = particlePool.removeAt(particlePool.size - 1)
            particle.x = x
            particle.y = y
            particle.velocityX = velocityX
            particle.velocityY = velocityY
            particle.color = color
            particle.life = life
            return particle
        }
        
        // Create new if pool is empty
        return Particle(x, y, velocityX, velocityY, color, life)
    }
    
    /**
     * Draws all particles.
     * 
     * @param canvas The canvas to draw on
     * @param paint The paint to use for drawing
     */
    fun draw(canvas: Canvas, paint: Paint) {
        for (particle in particles) {
            particle.draw(canvas, paint)
        }
    }
    
    /**
     * Creates particles at the given position with the given color.
     * 
     * @param x The x-coordinate to create particles at
     * @param y The y-coordinate to create particles at
     * @param color The color of the particles
     */
    fun createParticles(x: Float, y: Float, color: Int) {
        // Limit particles if we already have too many
        if (particles.size > 200) return
        
        for (i in 0 until particleCount) {
            val angle = Random.nextDouble(0.0, Math.PI * 2).toFloat()
            // Ensure minimum speed to prevent stationary particles
            val speed = Random.nextFloat() * particleMaxSpeed + 100f
            val velocityX = Math.cos(angle.toDouble()).toFloat() * speed
            val velocityY = Math.sin(angle.toDouble()).toFloat() * speed
            
            // Get particle from pool instead of creating new
            val particle = obtainParticle(x, y, velocityX, velocityY, color, particleLifespan)
            particles.add(particle)
        }
    }
    
    /**
     * Creates a special particle effect when the player loses a life.
     * 
     * @param x The x-coordinate to create the effect at
     * @param y The y-coordinate to create the effect at
     */
    fun createLifeLostEffect(x: Float, y: Float) {
        // Create red explosion particles
        val redColor = Color.rgb(255, 50, 50)
        
        // Limit particles if we already have too many
        if (particles.size > 150) return
        
        // Create more particles for a bigger explosion, but limit to 2 waves instead of 3
        for (i in 0 until 2) {
            for (j in 0 until particleCount) {
                val angle = Random.nextDouble(0.0, Math.PI * 2).toFloat()
                val speed = Random.nextFloat() * particleMaxSpeed * 1.5f + 150f // Ensure minimum speed
                val velocityX = Math.cos(angle.toDouble()).toFloat() * speed
                val velocityY = Math.sin(angle.toDouble()).toFloat() * speed
                
                // Get particle from pool instead of creating new
                val particle = obtainParticle(x, y, velocityX, velocityY, redColor, particleLifespan * 2)
                particles.add(particle)
            }
        }
    }
    
    /**
     * Clears all particles.
     */
    fun clear() {
        recycleAllParticles()
    }
    
    /**
     * Gets the current number of particles.
     * 
     * @return The number of particles
     */
    fun getParticleCount(): Int {
        return particles.size
    }
    
    /**
     * Particle class for visual effects.
     */
    private inner class Particle(
        var x: Float,
        var y: Float,
        var velocityX: Float,
        var velocityY: Float,
        var color: Int,
        var life: Int
    ) {
        fun update(deltaTime: Float) {
            x += velocityX * deltaTime
            y += velocityY * deltaTime
            velocityY += 25f * deltaTime // gravity
            velocityX *= (1 - 0.05f * deltaTime) // air resistance
            life--
            
            // If particle is almost dead, make it fade faster
            if (life < 5 && abs(velocityX) < 0.1f && abs(velocityY) < 0.1f) {
                life = 0
            }
        }
        
        fun draw(canvas: Canvas, paint: Paint) {
            // Fade out as life decreases
            val alpha = (255 * (life.toFloat() / particleLifespan)).toInt()
            paint.color = Color.argb(
                alpha,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
            canvas.drawCircle(x, y, particleSize * (life.toFloat() / particleLifespan), paint)
        }
    }
} 