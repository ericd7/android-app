package com.example.androidgame

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var soundSwitch: Switch
    private lateinit var saveButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        // Initialize views
        soundSwitch = findViewById(R.id.switchSound)
        saveButton = findViewById(R.id.buttonBack)
        
        // Load current settings
        val sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val soundEnabled = sharedPreferences.getBoolean("sound_enabled", true)
        
        // Set switch state based on saved preference
        soundSwitch.isChecked = soundEnabled
        
        // Set up save button
        saveButton.setOnClickListener {
            // Save settings
            sharedPreferences.edit()
                .putBoolean("sound_enabled", soundSwitch.isChecked)
                .apply()
            
            // Show confirmation toast
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            
            // Return to main menu
            finish()
        }
    }
} 