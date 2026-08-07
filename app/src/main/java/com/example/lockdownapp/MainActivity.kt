package com.studylockdown.app // ඔයාගේ ඇප් එකේ package name එක මෙතැනට දාන්න

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

    // Games සහ අනවශ්‍ය දේවල් block කරන Keywords ලැයිස්තුව
    private val blockedKeywords = listOf(
        "game", "pubg", "freefire", "movie", "song", "cartoons", "fun", "tiktok", "match", "play"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI Elements Bind කරගැනීම
        tvTimer = findViewById(R.id.tvTimer)
        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        etYtSearch = findViewById(R.id.etYtSearch)
        btnYtSearch = findViewById(R.id.btnYtSearch)
        tvYtResult = findViewById(R.id.tvYtResult)
        btnCallAmma = findViewById(R.id.btnCallAmma)
        btnCallThatha = findViewById(R.id.btnCallThatha)

        // මුලදී Pause button එක Disable කර තැබීම (Timer එක Run වෙනකම්)
        btnPause.isEnabled = false

        // 1. Start Timer Button
        btnStart.setOnClickListener {
            startTimer()
        }

        // 2. Pause Button (Timer එක Run වෙනවා නම් පමණක් වැඩ කරයි)
        btnPause.setOnClickListener {
            pauseTimer()
        }

        // 3. YouTube Educational Filter Search
        btnYtSearch.setOnClickListener {
            val query = etYtSearch.text.toString().lowercase().trim()
            validateYouTubeSearch(query)
        }

        // 4. Custom Emergency Calls
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
        btnPause.isEnabled = true // Timer එක පටන් ගත්තම Pause button එක Enable වෙනවා
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStart.isEnabled = true
        btnPause.isEnabled = false // Pause කළ පසු නැවත Pause button එක Disable වේ
        Toast.makeText(this, "Timer Paused", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimer.text = String.format("00:%02d:%02d", minutes, seconds)
    }

    // FULL LOCKDOWN: Timer එක Run වෙනකොට Back යන්න තහනම් කිරීම
    override fun onBackPressed() {
        if (isTimerRunning) {
            Toast.makeText(this, "🔒 Lockdown Active! You cannot exit until the timer finishes.", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    // YouTube Search Filter (Games Block Logic)
    private fun validateYouTubeSearch(query: String) {
        val isBlocked = blockedKeywords.any { query.contains(it) }
        
        if (isBlocked) {
            tvYtResult.text = "🚫 Access Blocked! Games and entertainment are not allowed during study lockdown."
        } else if (query.isEmpty()) {
            tvYtResult.text = "Please enter an educational topic."
        } else {
            tvYtResult.text = "✅ Showing educational results for: '$query'"
            // මෙතැනදී WebView හෝ YouTube Intent එකක් හරහා අධ්‍යාපනික වීඩියෝ ලෝඩ් කරගන්න පුළුවන්
        }
    }

    // Custom Emergency Call Function
    private fun makeEmergencyCall(key: String, defaultNum: String) {
        val prefs = getSharedPreferences("StudyAppPrefs", Context.MODE_PRIVATE)
        val number = prefs.getString(key, defaultNum)

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        }
        startActivity(intent)
    }
}

