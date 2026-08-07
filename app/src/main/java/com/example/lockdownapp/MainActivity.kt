package com.example.lockdownapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var btnEmergency: Button
    private lateinit var webViewYouTube: WebView

    private val blockedKeywords = listOf(
        "movie", "film", "song", "music", "gaming", "game", "minecraft", "pubg", 
        "freefire", "comedy", "tiktok", "dance", "prank", "trailer", "football", "cricket highlights"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContentView(R.layout.activity_main)
        
        timerTextView = findViewById(R.id.timerTextView)
        btnEmergency = findViewById(R.id.btnEmergency)
        webViewYouTube = findViewById(R.id.webViewYouTube)

        try {
            startLockTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startCountdownTimer(7200000)

        btnEmergency.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:119")
            }
            try {
                startActivity(callIntent)
            } catch (e: SecurityException) {
                Toast.makeText(this, "Call permission missing!", Toast.LENGTH_SHORT).show()
            }
        }

        setupYouTubeWebView()
    }

    private fun setupYouTubeWebView() {
        webViewYouTube.settings.javaScriptEnabled = true
        webViewYouTube.settings.domStorageEnabled = true
        webViewYouTube.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (isContentAllowed(url)) {
                    return false
                } else {
                    Toast.makeText(applicationContext, "Blocked! Educational content only.", Toast.LENGTH_LONG).show()
                    view?.loadUrl("https://www.youtube.com/results?search_query=educational+lessons")
                    return true
                }
            }
        }
        webViewYouTube.loadUrl("https://www.youtube.com/results?search_query=educational+lessons")
    }

    private fun isContentAllowed(url: String): Boolean {
        val lowerUrl = url.lowercase(Locale.ROOT)
        for (keyword in blockedKeywords) {
            if (lowerUrl.contains(keyword)) {
                return false
            }
        }
        return true
    }

    private fun startCountdownTimer(durationMillis: Long) {
        object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / 1000 / 3600
                val minutes = (millisUntilFinished / 1000 % 3600) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerTextView.text = String.format("LOCKDOWN | Left: %02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "UNLOCKED!"
                stopLockTask()
                finish()
            }
        }.start()
    }

    override fun onBackPressed() {
        if (webViewYouTube.canGoBack()) {
            webViewYouTube.goBack()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Int {
        if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_POWER) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
