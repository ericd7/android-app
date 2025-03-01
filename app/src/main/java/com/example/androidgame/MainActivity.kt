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
        binding.textGameTitle.text = "Android Game"
        
        // Set up button click listeners
        binding.buttonStart.setOnClickListener {
            // Start the game activity
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
        
        binding.buttonSettings.setOnClickListener {
            // TODO: Open settings screen
        }
    }
} 