package com.example.lockdownapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var etYtSearch: EditText
    private lateinit var btnYtSearch: Button
    private lateinit var tvYtResult: TextView
    private lateinit var btnCallAmma: Button
    private lateinit var btnCallThatha: Button

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 60000 // 1 Minute default
    private var isTimerRunning = false

    private val blockedKeywords = listOf(
        "game", "pubg", "freefire", "movie", "song", "cartoons", "fun", "tiktok", "match", "play"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTimer = findViewById(R.id.tvTimer)
        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        etYtSearch = findViewById(R.id.etYtSearch)
        btnYtSearch = findViewById(R.id.btnYtSearch)
        tvYtResult = findViewById(R.id.tvYtResult)
        btnCallAmma = findViewById(R.id.btnCallAmma)
        btnCallThatha = findViewById(R.id.btnCallThatha)

        btnPause.isEnabled = false

        btnStart.setOnClickListener {
            startTimer()
        }

        btnPause.setOnClickListener {
            pauseTimer()
        }

        btnYtSearch.setOnClickListener {
            val query = etYtSearch.text.toString().lowercase().trim()
            validateYouTubeSearch(query)
        }

        btnCallAmma.setOnClickListener {
            makeEmergencyCall("amma_number", "0712345678")
        }

        btnCallThatha.setOnClickListener {
            makeEmergencyCall("thatha_number", "0771234567")
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                isTimerRunning = false
                btnStart.isEnabled = true
                btnPause.isEnabled = false
                Toast.makeText(this@MainActivity, "🎉 Study session completed!", Toast.LENGTH_SHORT).show()
            }
        }.start()

        isTimerRunning = true
        btnStart.isEnabled = false
        btnPause.isEnabled = true
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStart.isEnabled = true
        btnPause.isEnabled = false
        Toast.makeText(this, "Timer Paused", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimer.text = String.format("00:%02d:%02d", minutes, seconds)
    }

    override fun onBackPressed() {
        if (isTimerRunning) {
            Toast.makeText(this, "🔒 Lockdown Active! You cannot exit until the timer finishes.", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    private fun validateYouTubeSearch(query: String) {
        val isBlocked = blockedKeywords.any { query.contains(it) }
        
        if (isBlocked) {
            tvYtResult.text = "🚫 Access Blocked! Games and entertainment are not allowed."
        } else if (query.isEmpty()) {
            tvYtResult.text = "Please enter an educational topic."
        } else {
            tvYtResult.text = "✅ Showing educational results for: '$query'"
        }
    }

    private fun makeEmergencyCall(key: String, defaultNum: String) {
        val prefs = getSharedPreferences("StudyAppPrefs", Context.MODE_PRIVATE)
        val number = prefs.getString(key, defaultNum)

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        }
        startActivity(intent)
    }
}

