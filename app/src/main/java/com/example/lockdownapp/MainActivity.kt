package com.example.lockdownapp

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private lateinit var tvTimer: TextView
    private lateinit var etMinutes: EditText
    private lateinit var btnSetTime: MaterialButton
    private lateinit var btnStart: MaterialButton
    private lateinit var btnPause: MaterialButton
    private lateinit var btnCallMom: MaterialButton
    private lateinit var btnCallDad: MaterialButton
    private lateinit var webViewYoutube: WebView

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 3600000 // Default 60 mins
    private var isTimerRunning = false
    private var isSessionCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTimer = findViewById(R.id.tvTimer)
        etMinutes = findViewById(R.id.etMinutes)
        btnSetTime = findViewById(R.id.btnSetTime)
        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnCallMom = findViewById(R.id.btnCallMom)
        btnCallDad = findViewById(R.id.btnCallDad)
        webViewYoutube = findViewById(R.id.webViewYoutube)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, MyAdminReceiver::class.java)

        // Request all permissions immediately upon app startup
        requestAllPermissions()

        setupYouTubePlayer()
        setupTimerControls()
        setupFamilyCalls()
    }

    private fun requestAllPermissions() {
        // Request Device Admin Permission
        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Admin permission is required to enforce study lockdown and prevent bypassing.")
            }
            startActivityForResult(intent, 100)
        }

        // Request Phone Call Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
        }
    }

    private fun setupYouTubePlayer() {
        webViewYoutube.settings.javaScriptEnabled = true
        webViewYoutube.settings.domStorageEnabled = true
        webViewYoutube.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webViewYoutube.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    if (it.contains("youtube.com") || it.contains("youtu.be")) {
                        view?.loadUrl(it)
                    } else {
                        Toast.makeText(applicationContext, "Only educational content is allowed.", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }
        }
        webViewYoutube.loadUrl("https://www.youtube.com/results?search_query=educational+lectures+science+math")
    }

    private fun setupTimerControls() {
        updateTimerText()

        btnSetTime.setOnClickListener {
            if (!isTimerRunning) {
                val inputStr = etMinutes.text.toString()
                if (inputStr.isNotEmpty()) {
                    val mins = inputStr.toLong()
                    timeLeftInMillis = mins * 60 * 1000
                    updateTimerText()
                    Toast.makeText(this, "Timer set to $mins minutes", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Cannot change time while timer is active!", Toast.LENGTH_SHORT).show()
            }
        }

        // Start and Resume combined into the same button functionality based on state
        btnStart.setOnClickListener {
            if (!isTimerRunning && !isSessionCompleted) {
                startTimer(timeLeftInMillis)
                btnStart.text = "Running..."
                btnStart.isEnabled = false
            }
        }

        // Pause functionality: Stops timer and keeps remaining time so it resumes from where it stopped
        btnPause.setOnClickListener {
            if (isTimerRunning) {
                countDownTimer?.cancel()
                isTimerRunning = false
                btnStart.text = "Resume"
                btnStart.isEnabled = true
                Toast.makeText(this, "Timer Paused. App remains locked.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startTimer(millis: Long) {
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                isTimerRunning = false
                isSessionCompleted = true
                tvTimer.text = "00:00:00"
                btnStart.text = "Completed"
                btnStart.isEnabled = false
                Toast.makeText(applicationContext, "Session Finished! App Unlocked.", Toast.LENGTH_LONG).show()
            }
        }.start()
        isTimerRunning = true
        Toast.makeText(this, "Study Lockdown Active!", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimerText() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun setupFamilyCalls() {
        btnCallMom.setOnClickListener {
            makePhoneCall("0712345678") // Replace with Amma's phone number
        }

        btnCallDad.setOnClickListener {
            makePhoneCall("0771234567") // Replace with Thatha's phone number
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (!isSessionCompleted) {
            Toast.makeText(this, "Study Lockdown Active! You cannot exit until session is completed.", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }
}
