package com.blockshooter.game.util

import android.graphics.RectF
import com.blockshooter.game.model.Ball
import com.blockshooter.game.model.Block
import kotlin.math.abs

/**
 * Utility class for collision detection in the game.
 */
object CollisionUtils {
    
    /**
     * Checks if a line segment intersects with a rectangle.
     * 
     * @param x1 The x-coordinate of the first point of the line segment
     * @param y1 The y-coordinate of the first point of the line segment
     * @param x2 The x-coordinate of the second point of the line segment
     * @param y2 The y-coordinate of the second point of the line segment
     * @param rect The rectangle to check for intersection
     * @return True if the line segment intersects with the rectangle, false otherwise
     */
    fun lineIntersectsRect(x1: Float, y1: Float, x2: Float, y2: Float, rect: RectF): Boolean {
        // Check if either endpoint is inside the rectangle
        if (rect.contains(x1, y1) || rect.contains(x2, y2)) {
            return true
        }
        
        // Check if the line intersects any of the rectangle's edges
        if (lineIntersectsLine(x1, y1, x2, y2, rect.left, rect.top, rect.right, rect.top) ||
            lineIntersectsLine(x1, y1, x2, y2, rect.right, rect.top, rect.right, rect.bottom) ||
            lineIntersectsLine(x1, y1, x2, y2, rect.right, rect.bottom, rect.left, rect.bottom) ||
            lineIntersectsLine(x1, y1, x2, y2, rect.left, rect.bottom, rect.left, rect.top)) {
            return true
        }
        
        return false
    }
    
    /**
     * Checks if two line segments intersect.
     * 
     * @param x1 The x-coordinate of the first point of the first line segment
     * @param y1 The y-coordinate of the first point of the first line segment
     * @param x2 The x-coordinate of the second point of the first line segment
     * @param y2 The y-coordinate of the second point of the first line segment
     * @param x3 The x-coordinate of the first point of the second line segment
     * @param y3 The y-coordinate of the first point of the second line segment
     * @param x4 The x-coordinate of the second point of the second line segment
     * @param y4 The y-coordinate of the second point of the second line segment
     * @return True if the line segments intersect, false otherwise
     */
    private fun lineIntersectsLine(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x3: Float, y3: Float, x4: Float, y4: Float
    ): Boolean {
        // Calculate the direction of the lines
        val uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) /
                ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1))
        val uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) /
                ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1))
        
        // If uA and uB are between 0-1, lines are colliding
        return uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1
    }
    
    /**
     * Determines which side of a block was hit by the ball.
     * 
     * @param ball The ball
     * @param block The block
     * @return A string indicating which side was hit: "top", "bottom", "left", "right", or "none"
     */
    fun getCollisionSide(ball: Ball, block: Block): String {
        val rect = block.rect
        
        // Calculate the center of the ball
        val ballCenterX = ball.x
        val ballCenterY = ball.y
        
        // Calculate the center of the block
        val blockCenterX = rect.left + (rect.right - rect.left) / 2
        val blockCenterY = rect.top + (rect.bottom - rect.top) / 2
        
        // Calculate the distance between the centers
        val dx = ballCenterX - blockCenterX
        val dy = ballCenterY - blockCenterY
        
        // Calculate the minimum distance to separate the ball from the block
        val minDistanceX = (rect.right - rect.left) / 2 + ball.radius
        val minDistanceY = (rect.bottom - rect.top) / 2 + ball.radius
        
        // Calculate the depth of penetration
        val depthX = minDistanceX - abs(dx)
        val depthY = minDistanceY - abs(dy)
        
        // Determine the side of collision based on the penetration depth
        return if (depthX < depthY) {
            if (dx > 0) "right" else "left"
        } else {
            if (dy > 0) "bottom" else "top"
        }
    }
} 