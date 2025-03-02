package com.blockshooter.game.effects

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Manages all sound effects in the game.
 */
class SoundManager(
    private var toneGenerator: ToneGenerator?,
    private val soundEnabled: Boolean
) {
    /**
     * Plays the sound when a regular block is hit.
     */
    fun playBlockHitSound() {
        if (!soundEnabled || toneGenerator == null) return
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
    }
    
    /**
     * Plays the sound when a special block is hit.
     */
    fun playSpecialBlockHitSound() {
        if (!soundEnabled || toneGenerator == null) return
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
    }
    
    /**
     * Plays the sound when the paddle hits the ball.
     */
    fun playPaddleHitSound() {
        if (!soundEnabled || toneGenerator == null) return
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
    }
    
    /**
     * Plays the sound when the ball is lost.
     */
    fun playBallLostSound() {
        if (!soundEnabled || toneGenerator == null) return
        
        // Play a more pleasant three-tone descending sequence
        Thread {
            try {
                // Higher pitch, shorter duration
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 70)
                Thread.sleep(80)
                
                // Medium pitch, medium duration
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                Thread.sleep(100)
                
                // Lower pitch, longer duration
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 130)
            } catch (e: Exception) {
                // Fallback if there's any issue with the sequence
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            }
        }.start()
    }
    
    /**
     * Plays the sound when the game is over.
     */
    fun playGameOverSound() {
        if (!soundEnabled || toneGenerator == null) return
        
        // Play a descending sequence for game over
        Thread {
            try {
                // Start with a higher pitch
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                Thread.sleep(200)
                
                // End with a lower pitch
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 300)
            } catch (e: Exception) {
                // Fallback
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200)
            }
        }.start()
    }
    
    /**
     * Plays the sound when a new level starts or a new row is added.
     */
    fun playLevelUpSound() {
        if (!soundEnabled || toneGenerator == null) return
        
        // Play an ascending sequence for level up
        Thread {
            try {
                // Start with a lower pitch
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
                Thread.sleep(70)
                
                // End with a higher pitch
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
            } catch (e: Exception) {
                // Fallback
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
            }
        }.start()
    }
    
    /**
     * Releases resources.
     */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
} 