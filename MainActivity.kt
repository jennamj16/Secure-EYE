package com.example.motioneyeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefsManager = PreferencesManager(this)

        val btnCameraAccess = findViewById<Button>(R.id.btnCameraAccess)
        val btnFootageAccess = findViewById<Button>(R.id.btnFootageAccess)
        val btnVerification = findViewById<Button>(R.id.btnVerification)
        val btnHashGenerator = findViewById<Button>(R.id.btnHashGenerator)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        btnCameraAccess.setOnClickListener {
            openWebView(prefsManager.getCameraUrl(), "Camera Access")
        }

        btnFootageAccess.setOnClickListener {
            openWebView(prefsManager.getFootageUrl(), "Footage Access")
        }

        btnVerification.setOnClickListener {
            openWebView(prefsManager.getVerificationUrl(), "Video Footage Verification")
        }

        btnHashGenerator.setOnClickListener {
            openHashGenerator()
        }

        btnSettings.setOnClickListener {
            openSettings()
        }
    }

    private fun openWebView(url: String, title: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra("URL", url)
            putExtra("TITLE", title)
        }
        startActivity(intent)
    }

    private fun openHashGenerator() {
        val intent = Intent(this, HashGeneratorActivity::class.java)
        startActivity(intent)
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}
