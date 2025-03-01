package com.example.androidgame

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

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
        
        // Create and set the game view
        gameView = GameView(this)
        setContentView(gameView)
    }
    
    override fun onResume() {
        super.onResume()
        // Start or resume the game
        gameView.startGame()
    }
    
    override fun onPause() {
        super.onPause()
        // Pause the game
    }
    
    override fun onBackPressed() {
        // Handle back button press
        // For example, show a dialog to confirm exit
        finish()
    }
} 