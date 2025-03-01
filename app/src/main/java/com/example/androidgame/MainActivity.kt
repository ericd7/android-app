package com.example.androidgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.androidgame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize your game here
        setupGame()
    }
    
    private fun setupGame() {
        // Set the game title
        binding.textGameTitle.text = "Block Shooter"
        
        // Set up button click listeners
        binding.buttonStart.setOnClickListener {
            // Start the game activity
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
        
        binding.buttonHighScores.setOnClickListener {
            // Open high scores screen
            val intent = Intent(this, HighScoresActivity::class.java)
            startActivity(intent)
        }
        
        binding.buttonSettings.setOnClickListener {
            // Open settings screen
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
} 