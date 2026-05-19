package com.example.motioneyeapp

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "MotionEyePrefs"
        private const val KEY_CAMERA_URL = "camera_url"
        private const val KEY_FOOTAGE_URL = "footage_url"
        private const val KEY_VERIFICATION_URL = "verification_url"
        
        // Default URLs
        const val DEFAULT_CAMERA_URL = "https://rpi.local/"
        const val DEFAULT_FOOTAGE_URL = "https://rpi.local/recordings"
        const val DEFAULT_VERIFICATION_URL = "https://rpi.local/verify/"
    }

    fun saveCameraUrl(url: String) {
        sharedPreferences.edit().putString(KEY_CAMERA_URL, url).apply()
    }

    fun saveFootageUrl(url: String) {
        sharedPreferences.edit().putString(KEY_FOOTAGE_URL, url).apply()
    }

    fun saveVerificationUrl(url: String) {
        sharedPreferences.edit().putString(KEY_VERIFICATION_URL, url).apply()
    }

    fun getCameraUrl(): String {
        return sharedPreferences.getString(KEY_CAMERA_URL, DEFAULT_CAMERA_URL) ?: DEFAULT_CAMERA_URL
    }

    fun getFootageUrl(): String {
        return sharedPreferences.getString(KEY_FOOTAGE_URL, DEFAULT_FOOTAGE_URL) ?: DEFAULT_FOOTAGE_URL
    }

    fun getVerificationUrl(): String {
        return sharedPreferences.getString(KEY_VERIFICATION_URL, DEFAULT_VERIFICATION_URL) ?: DEFAULT_VERIFICATION_URL
    }

    fun resetToDefaults() {
        sharedPreferences.edit().apply {
            putString(KEY_CAMERA_URL, DEFAULT_CAMERA_URL)
            putString(KEY_FOOTAGE_URL, DEFAULT_FOOTAGE_URL)
            putString(KEY_VERIFICATION_URL, DEFAULT_VERIFICATION_URL)
            apply()
        }
    }

    fun hasCustomUrls(): Boolean {
        return sharedPreferences.contains(KEY_CAMERA_URL) || 
               sharedPreferences.contains(KEY_FOOTAGE_URL) ||
               sharedPreferences.contains(KEY_VERIFICATION_URL)
    }
}
