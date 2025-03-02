package com.blockshooter.game.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.blockshooter.game.effects.GlowEffect
import com.blockshooter.game.effects.ParticleSystem
import com.blockshooter.game.effects.ScreenShakeEffect
import com.blockshooter.game.model.Ball
import com.blockshooter.game.model.Block

/**
 * Handles rendering of game elements.
 */
class GameRenderer(
    private val paint: Paint,
    private val particleSystem: ParticleSystem,
    private val glowEffect: GlowEffect,
    private val screenShakeEffect: ScreenShakeEffect
) {
    // Visual settings
    private val blockCornerRadius = 8f
    private val blockShadowOffset = 4f
    private val blockShadowColor = Color.argb(100, 0, 0, 0) // Semi-transparent black for shadows
    private val blockGradientFactor = 0.8f // Factor to darken the bottom of blocks for gradient effect
    private val paddleCornerRadius = 15f // Larger corner radius for paddle
    private val ballGradientRadius = 0.7f // Factor for ball gradient
    
    /**
     * Draws all game elements on the canvas.
     * 
     * @param canvas The canvas to draw on
     * @param backgroundColor The background color of the game
     * @param blocks The list of blocks to draw
     * @param paddle The paddle to draw
     * @param ball The ball to draw
     * @param score The current score
     * @param lives The number of lives remaining
     * @param gameOver Whether the game is over
     * @param isTopScore Whether the current score is a top score
     * @param restartButton The restart button rectangle (for game over screen)
     * @param backButton The back button rectangle (for game over screen)
     * @param ballWaitingForLaunch Whether the ball is waiting for the player to tap to launch
     */
    fun draw(
        canvas: Canvas,
        backgroundColor: Int,
        blocks: List<Block>,
        paddle: RectF,
        ball: Ball,
        score: Int,
        lives: Int,
        gameOver: Boolean = false,
        isTopScore: Boolean = false,
        restartButton: RectF? = null,
        backButton: RectF? = null,
        ballWaitingForLaunch: Boolean = false
    ) {
        // Apply screen shake effect
        screenShakeEffect.update()
        canvas.translate(screenShakeEffect.offsetX, screenShakeEffect.offsetY)
        
        // Draw background
        canvas.drawColor(backgroundColor)
        
        // Draw blocks
        drawBlocks(canvas, blocks)
        
        // Draw paddle
        drawPaddle(canvas, paddle)
        
        // Draw ball
        drawBall(canvas, ball)
        
        // Draw particles
        particleSystem.draw(canvas, paint)
        
        // Draw score and lives
        drawHUD(canvas, score, lives)
        
        // Draw game over screen if needed
        if (gameOver) {
            drawGameOverScreen(canvas, score, isTopScore, restartButton, backButton)
        }
        
        // Draw tap to launch message if ball is waiting for launch
        if (ballWaitingForLaunch) {
            drawTapToLaunchMessage(canvas)
        }
        
        // Reset canvas translation from screen shake
        canvas.translate(-screenShakeEffect.offsetX, -screenShakeEffect.offsetY)
    }
    
    /**
     * Draws all blocks on the canvas.
     * 
     * @param canvas The canvas to draw on
     * @param blocks The list of blocks to draw
     */
    private fun drawBlocks(canvas: Canvas, blocks: List<Block>) {
        for (block in blocks) {
            // Skip destroyed blocks
            if (block.isDestroyed) continue
            
            // Draw glow effect for special blocks
            if (block.isSpecial) {
                glowEffect.drawGlow(canvas, paint, block.rect, blockCornerRadius, Color.argb(255, 200, 230, 255))
            }
            
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
                (Color.red(block.color) * blockGradientFactor).toInt(),
                (Color.green(block.color) * blockGradientFactor).toInt(),
                (Color.blue(block.color) * blockGradientFactor).toInt()
            )
            val gradientRect = RectF(
                block.rect.left,
                block.rect.top + block.rect.height() / 2,
                block.rect.right,
                block.rect.bottom
            )
            canvas.drawRoundRect(gradientRect, 0f, blockCornerRadius, paint)
        }
    }
    
    /**
     * Draws the paddle on the canvas.
     * 
     * @param canvas The canvas to draw on
     * @param paddle The paddle rectangle to draw
     */
    private fun drawPaddle(canvas: Canvas, paddle: RectF) {
        // Draw paddle shadow
        paint.color = blockShadowColor
        val paddleShadowRect = RectF(
            paddle.left + blockShadowOffset,
            paddle.top + blockShadowOffset,
            paddle.right + blockShadowOffset,
            paddle.bottom + blockShadowOffset
        )
        canvas.drawRoundRect(paddleShadowRect, paddleCornerRadius, paddleCornerRadius, paint)
        
        // Draw paddle
        paint.color = Color.WHITE
        canvas.drawRoundRect(paddle, paddleCornerRadius, paddleCornerRadius, paint)
        
        // Draw paddle gradient (darker at bottom)
        paint.color = Color.argb(80, 150, 150, 150) // Semi-transparent gray
        val paddleGradientRect = RectF(
            paddle.left,
            paddle.top + paddle.height() / 2,
            paddle.right,
            paddle.bottom
        )
        canvas.drawRoundRect(paddleGradientRect, 0f, paddleCornerRadius, paint)
    }
    
    /**
     * Draws the ball on the canvas.
     * 
     * @param canvas The canvas to draw on
     * @param ball The ball to draw
     */
    private fun drawBall(canvas: Canvas, ball: Ball) {
        // Draw ball shadow
        paint.color = blockShadowColor
        canvas.drawCircle(
            ball.x + blockShadowOffset / 2,
            ball.y + blockShadowOffset / 2,
            ball.radius,
            paint
        )
        
        // Draw ball
        paint.color = Color.WHITE
        canvas.drawCircle(ball.x, ball.y, ball.radius, paint)
        
        // Draw ball gradient (darker at bottom-right)
        paint.color = Color.argb(100, 150, 150, 150) // Semi-transparent gray
        canvas.drawCircle(
            ball.x + ball.radius * 0.2f,
            ball.y + ball.radius * 0.2f,
            ball.radius * ballGradientRadius,
            paint
        )
    }
    
    /**
     * Draws the HUD (score and lives) on the canvas.
     * 
     * @param canvas The canvas to draw on
     * @param score The current score
     * @param lives The number of lives remaining
     */
    private fun drawHUD(canvas: Canvas, score: Int, lives: Int) {
        // Draw score
        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Score: $score", 20f, 50f, paint)
        
        // Draw lives
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Lives: $lives", canvas.width - 20f, 50f, paint)
    }
    
    /**
     * Draws the game over screen on the canvas.
     * 
     * @param canvas The canvas to draw on
     * @param score The final score
     * @param isTopScore Whether the score is a top score
     * @param restartButton The restart button rectangle
     * @param backButton The back button rectangle
     */
    private fun drawGameOverScreen(
        canvas: Canvas,
        score: Int,
        isTopScore: Boolean,
        restartButton: RectF?,
        backButton: RectF?
    ) {
        // Draw semi-transparent overlay
        paint.color = Color.argb(180, 0, 0, 0)
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        
        // Draw game over text
        paint.color = Color.WHITE
        paint.textSize = 60f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("GAME OVER", canvas.width / 2f, canvas.height / 3f, paint)
        
        // Draw score
        paint.textSize = 40f
        canvas.drawText("Score: $score", canvas.width / 2f, canvas.height / 3f + 60f, paint)
        
        // Draw top score message if applicable
        if (isTopScore) {
            paint.color = Color.YELLOW
            canvas.drawText("NEW HIGH SCORE!", canvas.width / 2f, canvas.height / 3f + 120f, paint)
        }
        
        // Draw buttons if provided
        if (restartButton != null && backButton != null) {
            // Draw restart button
            paint.color = Color.rgb(0, 150, 0) // Green
            canvas.drawRoundRect(restartButton, 15f, 15f, paint)
            
            paint.color = Color.WHITE
            paint.textSize = 30f
            canvas.drawText(
                "RESTART",
                restartButton.centerX(),
                restartButton.centerY() + 10f,
                paint
            )
            
            // Draw back button
            paint.color = Color.rgb(150, 0, 0) // Red
            canvas.drawRoundRect(backButton, 15f, 15f, paint)
            
            paint.color = Color.WHITE
            canvas.drawText(
                "MENU",
                backButton.centerX(),
                backButton.centerY() + 10f,
                paint
            )
        }
    }
    
    /**
     * Draws a message instructing the player to tap to launch the ball.
     * 
     * @param canvas The canvas to draw on
     */
    private fun drawTapToLaunchMessage(canvas: Canvas) {
        // Draw semi-transparent overlay
        paint.color = Color.argb(100, 0, 0, 0)
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        
        // Draw message
        paint.color = Color.WHITE
        paint.textSize = 50f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("TAP TO LAUNCH", canvas.width / 2f, canvas.height / 2f, paint)
    }
} 