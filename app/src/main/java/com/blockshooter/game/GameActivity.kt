package com.blockshooter.game

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameActivity : AppCompatActivity() {
    
    private lateinit var gameView: GameView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set fullscreen using modern approach
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        // Use the modern approach for fullscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) approach using WindowCompat
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Use WindowInsetsControllerCompat for better compatibility
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // Legacy approach for older Android versions
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
        
        // Create and set the game view
        gameView = GameView(this)
        setContentView(gameView)
        
        // Set game over listener to save score
        gameView.setOnGameOverListener { score ->
            if (score > 0) {
                val isTopScore = saveHighScore(score)
                gameView.setIsTopScore(isTopScore)
            }
        }
    }
    
    private fun saveHighScore(score: Int): Boolean {
        val sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val currentScores = sharedPreferences.getString("high_scores", "") ?: ""
        
        // Format current date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        // Parse existing scores
        val scores = mutableListOf<Pair<Int, String>>()
        if (currentScores.isNotEmpty()) {
            val scoreEntries = currentScores.split(";")
            for (entry in scoreEntries) {
                if (entry.isNotEmpty()) {
                    val parts = entry.split(",")
                    if (parts.size == 2) {
                        val scoreValue = parts[0].toIntOrNull()
                        val date = parts[1]
                        if (scoreValue != null) {
                            scores.add(Pair(scoreValue, date))
                        }
                    }
                }
            }
        }
        
        // Add new score
        scores.add(Pair(score, currentDate))
        
        // Sort scores in descending order
        scores.sortByDescending { it.first }
        
        // Keep only top 10 scores
        val topScores = scores.take(10)
        
        // Check if current score is the top score
        val isTopScore = topScores.isNotEmpty() && topScores[0].first == score
        
        // Convert back to string format
        val updatedScores = topScores.joinToString(";") { "${it.first},${it.second}" }
        
        // Save updated scores
        sharedPreferences.edit().putString("high_scores", updatedScores).apply()
        
        return isTopScore
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