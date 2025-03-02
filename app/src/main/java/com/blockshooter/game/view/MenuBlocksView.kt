package com.blockshooter.game.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.blockshooter.game.R

/**
 * A custom view that draws decorative blocks at the top of the main menu screen.
 */
class MenuBlocksView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val blocks = mutableListOf<Block>()
    
    // Block properties
    private val blockRows = 5
    private val blockCols = 8
    private val blockMargin = 8f
    private val blockCornerRadius = 8f
    private val blockShadowOffset = 4f
    private val blockShadowColor = Color.argb(100, 0, 0, 0) // Semi-transparent black for shadows
    
    // Rainbow colors for blocks
    private val rainbowColors = arrayOf(
        Color.rgb(255, 0, 0),      // Red
        Color.rgb(255, 127, 0),    // Orange
        Color.rgb(255, 255, 0),    // Yellow
        Color.rgb(0, 255, 0),      // Green
        Color.rgb(0, 0, 255),      // Blue
        Color.rgb(75, 0, 130),     // Indigo
        Color.rgb(148, 0, 211)     // Violet
    )
    
    // Special block color
    private val specialBlockColor = Color.rgb(240, 248, 255) // Alice Blue (slightly blue-tinted white)
    
    // Data class for blocks
    private data class Block(val rect: RectF, val color: Int, val isSpecial: Boolean = false)
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createBlocks(w)
    }
    
    /**
     * Creates the decorative blocks.
     */
    private fun createBlocks(width: Int) {
        blocks.clear()
        
        val blockHeight = 40f
        val blockWidth = (width - (blockCols + 1) * blockMargin) / blockCols
        
        for (row in 0 until blockRows) {
            // Get color for this row from rainbow colors
            val rowColor = rainbowColors[row % rainbowColors.size]
            
            for (col in 0 until blockCols) {
                val left = (col + 1) * blockMargin + col * blockWidth
                val top = blockMargin + row * (blockHeight + blockMargin)
                val right = left + blockWidth
                val bottom = top + blockHeight
                
                // 5% chance for a special white block
                val isSpecial = Math.random() < 0.05
                
                // Use rainbow color for this row, or white for special blocks
                val blockColor = if (isSpecial) specialBlockColor else rowColor
                
                blocks.add(Block(RectF(left, top, right, bottom), blockColor, isSpecial))
            }
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        for (block in blocks) {
            // Draw block shadow
            paint.color = blockShadowColor
            val shadowRect = RectF(
                block.rect.left + blockShadowOffset,
                block.rect.top + blockShadowOffset,
                block.rect.right + blockShadowOffset,
                block.rect.bottom + blockShadowOffset
            )
            canvas.drawRoundRect(shadowRect, blockCornerRadius, blockCornerRadius, paint)
            
            // Draw block
            paint.color = block.color
            canvas.drawRoundRect(block.rect, blockCornerRadius, blockCornerRadius, paint)
            
            // Draw gradient effect (darker at bottom)
            paint.color = Color.argb(
                80, // Semi-transparent
                (Color.red(block.color) * 0.8f).toInt(),
                (Color.green(block.color) * 0.8f).toInt(),
                (Color.blue(block.color) * 0.8f).toInt()
            )
            val gradientRect = RectF(
                block.rect.left,
                block.rect.top + block.rect.height() / 2,
                block.rect.right,
                block.rect.bottom
            )
            canvas.drawRoundRect(gradientRect, 0f, blockCornerRadius, paint)
            
            // Draw glow effect for special blocks
            if (block.isSpecial) {
                paint.color = Color.argb(50, 255, 255, 255)
                val glowRect = RectF(
                    block.rect.left - 4f,
                    block.rect.top - 4f,
                    block.rect.right + 4f,
                    block.rect.bottom + 4f
                )
                canvas.drawRoundRect(glowRect, blockCornerRadius + 4f, blockCornerRadius + 4f, paint)
            }
        }
    }
} 