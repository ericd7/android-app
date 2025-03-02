package com.blockshooter.game

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup

class HighScoresActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HighScoreAdapter
    private lateinit var backButton: Button
    private lateinit var noScoresText: TextView
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_scores)
        
        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewHighScores)
        backButton = findViewById(R.id.buttonBack)
        noScoresText = findViewById(R.id.textNoScores)
        
        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Get high scores from SharedPreferences
        sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val highScores = getHighScores()
        
        // Set up adapter
        adapter = HighScoreAdapter(highScores)
        recyclerView.adapter = adapter
        
        // Show "No scores yet" message if there are no scores
        if (highScores.isEmpty()) {
            noScoresText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noScoresText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
        
        // Set up back button
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun getHighScores(): List<HighScore> {
        val scores = mutableListOf<HighScore>()
        val scoresString = sharedPreferences.getString("high_scores", "")
        
        if (!scoresString.isNullOrEmpty()) {
            val scoreEntries = scoresString.split(";")
            for (entry in scoreEntries) {
                if (entry.isNotEmpty()) {
                    val parts = entry.split(",")
                    if (parts.size == 2) {
                        val score = parts[0].toIntOrNull()
                        val date = parts[1]
                        if (score != null) {
                            scores.add(HighScore(score, date))
                        }
                    }
                }
            }
        }
        
        // Sort scores in descending order
        return scores.sortedByDescending { it.score }
    }
}

// Data class for high scores
data class HighScore(val score: Int, val date: String)

// Adapter for the RecyclerView
class HighScoreAdapter(private val scores: List<HighScore>) : 
    RecyclerView.Adapter<HighScoreAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankText: TextView = view.findViewById(R.id.textRank)
        val scoreText: TextView = view.findViewById(R.id.textScore)
        val dateText: TextView = view.findViewById(R.id.textDate)
        val cardView: androidx.cardview.widget.CardView = view as androidx.cardview.widget.CardView
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_high_score, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val highScore = scores[position]
        holder.rankText.text = "#${position + 1}"
        holder.scoreText.text = "${highScore.score}"
        holder.dateText.text = highScore.date
        
        // Alternate between green and white backgrounds
        val context = holder.itemView.context
        if (position % 2 == 0) {
            // Even positions - use green background
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.game_primary))
            holder.rankText.setTextColor(context.getColor(R.color.game_secondary))
            holder.scoreText.setTextColor(context.getColor(R.color.game_secondary))
            holder.dateText.setTextColor(context.getColor(R.color.game_secondary))
        } else {
            // Odd positions - use white background
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.white))
            holder.rankText.setTextColor(context.getColor(R.color.game_primary))
            holder.scoreText.setTextColor(context.getColor(R.color.game_primary))
            holder.dateText.setTextColor(context.getColor(R.color.game_primary))
        }
    }
    
    override fun getItemCount() = scores.size
} 