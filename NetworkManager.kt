package com.example.motioneyeapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Utility class for network operations
 * Demonstrates Kotlin-Java interoperability by using the Java SSLCertificateHandler
 */
class NetworkManager(private val context: Context) {

    /**
     * Check if device has internet connectivity
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo?.isConnected == true
        }
    }

    /**
     * Check if connected to WiFi
     */
    fun isWifiConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
    }

    /**
     * Check if a URL is a local address using Java utility
     * Demonstrates Kotlin calling Java code
     */
    fun isLocalUrl(url: String): Boolean {
        val hostname = extractHostname(url)
        // Call Java utility method from Kotlin
        return SSLCertificateHandler.isLocalHost(hostname)
    }

    private fun extractHostname(url: String): String {
        return try {
            val cleanUrl = url.replace("https://", "").replace("http://", "")
            cleanUrl.substringBefore("/").substringBefore(":")
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        const val CONNECTION_TIMEOUT = 10000 // 10 seconds
        const val READ_TIMEOUT = 15000 // 15 seconds
    }
}
