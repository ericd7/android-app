package com.blockshooter.game

import android.content.Context
import android.os.Bundle
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.blockshooter.game.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var soundSwitch: Switch
    private lateinit var saveButton: Button
    
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
    
    override fun onResume() {
        super.onResume()
        
        // Re-apply fullscreen when returning to this activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }
} 