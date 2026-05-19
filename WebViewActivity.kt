package com.example.motioneyeapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var currentUrl: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        // Get URL and title from intent
        currentUrl = intent.getStringExtra("URL")
        val title = intent.getStringExtra("TITLE")

        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(true)
        }

        setupWebView()

        currentUrl?.let {
            webView.loadUrl(it)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            // Enable JavaScript
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true

            // Enable caching
            cacheMode = WebSettings.LOAD_DEFAULT
            setAppCacheEnabled(true)

            // Enable zooming
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)

            // Mixed content
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // Media playback
            mediaPlaybackRequiresUserGesture = false

            // Set user agent
            userAgentString = "$userAgentString MotionEyeApp/1.0"
        }

        // WebViewClient to handle SSL and authentication
        webView.webViewClient = object : WebViewClient() {

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                // Trust self-signed certificates from rpi.local
                if (currentUrl?.contains("rpi.local") == true) {
                    handler?.proceed() // Accept SSL certificate
                } else {
                    // Show dialog for other domains
                    AlertDialog.Builder(this@WebViewActivity)
                        .setTitle("SSL Certificate Error")
                        .setMessage("The SSL certificate is not trusted. Do you want to continue?")
                        .setPositiveButton("Continue") { _, _ -> handler?.proceed() }
                        .setNegativeButton("Cancel") { _, _ -> handler?.cancel() }
                        .show()
                }
            }

            override fun onReceivedHttpAuthRequest(
                view: WebView?,
                handler: HttpAuthHandler?,
                host: String?,
                realm: String?
            ) {
                // Show authentication dialog
                handler?.let { showAuthDialog(it) }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // Stay within the WebView
                val url = request?.url.toString()
                if (url.contains("rpi.local")) {
                    view?.loadUrl(url)
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    Toast.makeText(
                        this@WebViewActivity,
                        "Error loading page: ${error?.description}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // WebChromeClient for progress
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun showAuthDialog(handler: HttpAuthHandler) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Authentication Required")

        // Create input fields
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val usernameInput = EditText(this).apply {
            hint = "Username"
        }
        layout.addView(usernameInput)

        val passwordInput = EditText(this).apply {
            hint = "Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(passwordInput)

        builder.setView(layout)

        builder.setPositiveButton("Login") { _, _ ->
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            handler.proceed(username, password)
        }

        builder.setNegativeButton("Cancel") { _, _ ->
            handler.cancel()
            finish()
        }

        builder.setCancelable(false)
        builder.show()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
