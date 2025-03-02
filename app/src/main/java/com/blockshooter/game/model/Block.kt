package com.blockshooter.game.model

import android.graphics.RectF

/**
 * Represents a block in the game.
 * 
 * @property rect The rectangle defining the block's position and size
 * @property color The color of the block
 * @property isSpecial Whether this is a special block with unique properties
 * @property health The number of hits required to destroy the block
 */
class Block(
    val rect: RectF,
    val color: Int,
    val isSpecial: Boolean = false,
    var health: Int = 1
) {
    // Whether this block has been hit and is in the process of being destroyed
    var isDestroyed = false
    
    // Animation properties for destruction effect
    var destroyProgress = 0f
    
    /**
     * Damages the block, reducing its health by 1.
     * @return True if the block is destroyed (health <= 0), false otherwise
     */
    fun damage(): Boolean {
        health--
        return health <= 0
    }
    
    /**
     * Checks if this block intersects with the given coordinates.
     */
    fun contains(x: Float, y: Float): Boolean {
        return rect.contains(x, y)
    }
    
    /**
     * Creates a copy of this block.
     */
    fun copy(): Block {
        return Block(
            RectF(rect),
            color,
            isSpecial,
            health
        ).also {
            it.isDestroyed = isDestroyed
            it.destroyProgress = destroyProgress
        }
    }
} 