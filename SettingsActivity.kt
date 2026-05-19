package com.example.motioneyeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var etCameraUrl: EditText
    private lateinit var etFootageUrl: EditText
    private lateinit var etVerificationUrl: EditText
    private lateinit var btnSave: Button
    private lateinit var btnReset: Button
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.apply {
            title = "Settings"
            setDisplayHomeAsUpEnabled(true)
        }

        prefsManager = PreferencesManager(this)
        
        initViews()
        loadCurrentSettings()
        setupListeners()
    }

    private fun initViews() {
        etCameraUrl = findViewById(R.id.etCameraUrl)
        etFootageUrl = findViewById(R.id.etFootageUrl)
        etVerificationUrl = findViewById(R.id.etVerificationUrl)
        btnSave = findViewById(R.id.btnSave)
        btnReset = findViewById(R.id.btnReset)
    }

    private fun loadCurrentSettings() {
        etCameraUrl.setText(prefsManager.getCameraUrl())
        etFootageUrl.setText(prefsManager.getFootageUrl())
        etVerificationUrl.setText(prefsManager.getVerificationUrl())
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveSettings()
        }

        btnReset.setOnClickListener {
            resetToDefaults()
        }
    }

    private fun saveSettings() {
        val cameraUrl = etCameraUrl.text.toString().trim()
        val footageUrl = etFootageUrl.text.toString().trim()
        val verificationUrl = etVerificationUrl.text.toString().trim()

        // Validation
        if (cameraUrl.isEmpty()) {
            etCameraUrl.error = "Camera URL is required"
            etCameraUrl.requestFocus()
            return
        }

        if (footageUrl.isEmpty()) {
            etFootageUrl.error = "Footage URL is required"
            etFootageUrl.requestFocus()
            return
        }

        if (verificationUrl.isEmpty()) {
            etVerificationUrl.error = "Verification URL is required"
            etVerificationUrl.requestFocus()
            return
        }

        if (!isValidUrl(cameraUrl)) {
            etCameraUrl.error = "Invalid URL format"
            etCameraUrl.requestFocus()
            return
        }

        if (!isValidUrl(footageUrl)) {
            etFootageUrl.error = "Invalid URL format"
            etFootageUrl.requestFocus()
            return
        }

        if (!isValidUrl(verificationUrl)) {
            etVerificationUrl.error = "Invalid URL format"
            etVerificationUrl.requestFocus()
            return
        }

        // Save to preferences
        prefsManager.saveCameraUrl(cameraUrl)
        prefsManager.saveFootageUrl(footageUrl)
        prefsManager.saveVerificationUrl(verificationUrl)

        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetToDefaults() {
        prefsManager.resetToDefaults()
        loadCurrentSettings()
        Toast.makeText(this, "Reset to default URLs", Toast.LENGTH_SHORT).show()
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
