package com.lockdown.app

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
    private var timeLeftInMillis: Long = 60000 // 1 Minute
    private var isTimerRunning = false

    // Blocked Keywords for Games & Entertainment
    private val blockedKeywords = listOf("game", "pubg", "freefire", "movie", "song", "cartoons", "fun", "tiktok", "match")

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

        // 1. Timer Start Button
        btnStart.setOnClickListener {
            startTimer()
        }

        // 2. Timer Pause Button (Works ONLY when running)
        btnPause.setOnClickListener {
            pauseTimer()
        }

        // 3. YouTube Search Filter
        btnYtSearch.setOnClickListener {
            val query = etYtSearch.text.toString().lowercase().trim()
            validateYouTubeSearch(query)
        }

        // 4. Emergency Calls (Custom Numbers from SharedPreferences)
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
                Toast.classInvokeSafely("Study session completed!")
            }
        }.start()

        isTimerRunning = true
        btnStart.isEnabled = false
        btnPause.isEnabled = true // Enable Pause ONLY when running
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStart.isEnabled = true
        btnPause.isEnabled = false // Disable when paused
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimer.text = String.format("%02d:%02d:%02d", 0, minutes, seconds)
    }

    // FULL LOCKDOWN: Back Button Block when Timer is Running
    override fun onBackPressed() {
        if (isTimerRunning) {
            Toast.makeText(this, "🔒 Lockdown Active! You cannot exit or go back.", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    private fun validateYouTubeSearch(query: String) {
        val isBlocked = blockedKeywords.any { query.contains(it) }
        if (isBlocked) {
            tvYtResult.text = "🚫 Access Blocked! Games and fun are not allowed."
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
