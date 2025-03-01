package com.example.androidgame

import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameActivity : AppCompatActivity() {
    
    private lateinit var gameView: GameView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // Hide the status bar for immersive experience
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        )
        
        // Create and set the game view
        gameView = GameView(this)
        setContentView(gameView)
        
        // Set game over listener to save score
        gameView.setOnGameOverListener { score ->
            if (score > 0) {
                saveHighScore(score)
            }
        }
    }
    
    private fun saveHighScore(score: Int) {
        val sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val currentScores = sharedPreferences.getString("high_scores", "") ?: ""
        
        // Format current date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        // Add new score
        val newScore = "$score,$currentDate"
        val updatedScores = if (currentScores.isEmpty()) {
            newScore
        } else {
            "$currentScores;$newScore"
        }
        
        // Save updated scores
        sharedPreferences.edit().putString("high_scores", updatedScores).apply()
    }
    
    override fun onResume() {
        super.onResume()
        // The game will start when the player taps the screen
        // This is handled in the GameView's onTouchEvent
    }
    
    override fun onPause() {
        super.onPause()
        // Pause the game
        gameView.endGame()
    }
    
    override fun onBackPressed() {
        // Return to main menu
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Release sound resources
        gameView.release()
    }
} 