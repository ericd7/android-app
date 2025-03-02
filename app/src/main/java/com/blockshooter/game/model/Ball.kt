package com.blockshooter.game.model

import android.graphics.RectF
import kotlin.math.sqrt

/**
 * Represents the ball in the game.
 * 
 * @property x The x-coordinate of the ball's center
 * @property y The y-coordinate of the ball's center
 * @property radius The radius of the ball
 * @property velocityX The horizontal velocity of the ball
 * @property velocityY The vertical velocity of the ball
 */
class Ball(
    var x: Float,
    var y: Float,
    val radius: Float,
    var velocityX: Float,
    var velocityY: Float
) {
    // The maximum speed the ball can travel
    private val maxSpeed = 1200f
    
    /**
     * Updates the ball's position based on its velocity.
     * @param deltaTime The time elapsed since the last update in seconds
     */
    fun update(deltaTime: Float) {
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        
        // Ensure the ball doesn't exceed maximum speed
        limitSpeed()
    }
    
    /**
     * Limits the ball's speed to the maximum allowed value.
     */
    private fun limitSpeed() {
        val speed = sqrt(velocityX * velocityX + velocityY * velocityY)
        if (speed > maxSpeed) {
            val ratio = maxSpeed / speed
            velocityX *= ratio
            velocityY *= ratio
        }
    }
    
    /**
     * Checks if the ball intersects with the given rectangle.
     * @param rect The rectangle to check for intersection
     * @return True if the ball intersects with the rectangle, false otherwise
     */
    fun intersects(rect: RectF): Boolean {
        // Find the closest point on the rectangle to the ball's center
        val closestX = x.coerceIn(rect.left, rect.right)
        val closestY = y.coerceIn(rect.top, rect.bottom)
        
        // Calculate the distance between the ball's center and this closest point
        val distanceX = x - closestX
        val distanceY = y - closestY
        
        // If the distance is less than the ball's radius, an intersection occurs
        return (distanceX * distanceX + distanceY * distanceY) < (radius * radius)
    }
    
    /**
     * Creates a copy of this ball.
     */
    fun copy(): Ball {
        return Ball(x, y, radius, velocityX, velocityY)
    }
} 