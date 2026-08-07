package com.example.lockdownapp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private lateinit var tvTimer: TextView
    private lateinit var btnStart: MaterialButton
    private lateinit var btnPause: MaterialButton
    private lateinit var btnEmergency: MaterialButton
    private lateinit var webViewYoutube: WebView

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 3600000 // Default 1 hour educational session
    private var isTimerRunning = false
    private var isSessionCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTimer = findViewById(R.id.tvTimer)
        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnEmergency = findViewById(R.id.btnEmergency)
        webViewYoutube = findViewById(R.id.webViewYoutube)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, MyAdminReceiver::class.java)

        checkDeviceAdminPermission()
        setupYouTubePlayer()
        setupTimerLogic()
        setupEmergencySection()
    }

    private fun checkDeviceAdminPermission() {
        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app requires Administrator permissions to enforce the lockdown state and prevent bypassing.")
            }
            startActivityForResult(intent, 100)
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
                        Toast.makeText(applicationContext, "Non-educational links are blocked.", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }
        }
        webViewYoutube.loadUrl("https://www.youtube.com/results?search_query=educational+lectures+stem")
    }

    private fun setupTimerLogic() {
        btnStart.setOnClickListener {
            if (!isTimerRunning) {
                startTimer()
            }
        }

        btnPause.setOnClickListener {
            showRestrictedActionDialog()
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
                isSessionCompleted = true
                tvTimer.text = "00:00:00"
                Toast.makeText(applicationContext, "Study Session Completed! App Unlocked.", Toast.LENGTH_LONG).show()
                btnStart.isEnabled = false
            }
        }.start()
        isTimerRunning = true
        Toast.makeText(this, "Lockdown Started. App cannot be closed.", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimerText() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun showRestrictedActionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Restriction Active")
            .setMessage("You cannot stop or pause the educational timer until the full session countdown finishes.")
            .setPositiveButton("Understood", null)
            .show()
    }

    private fun setupEmergencySection() {
        btnEmergency.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Emergency Call Portal")
            builder.setMessage("Select an emergency action:")
            builder.setPositiveButton("Call Emergency Services") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:119")
                }
                startActivity(intent)
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }
    }

    override fun onBackPressed() {
        if (!isSessionCompleted) {
            Toast.makeText(this, "Study Lockdown Active. Complete the timer to exit.", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }
}
