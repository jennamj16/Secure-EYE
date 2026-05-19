package com.example.motioneyeapp

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.InputStream
import java.security.MessageDigest

class HashGeneratorActivity : AppCompatActivity() {

    private lateinit var tvFileName: TextView
    private lateinit var tvFileSize: TextView
    private lateinit var tvHashResult: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var btnCopyHash: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    private var selectedFileUri: Uri? = null
    private var currentHash: String? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 100
        private const val REQUEST_FILE_PICKER = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hash_generator)

        supportActionBar?.apply {
            title = "Hash Generator"
            setDisplayHomeAsUpEnabled(true)
        }

        initViews()
        setupListeners()
        checkAndRequestPermissions()
    }

    private fun initViews() {
        tvFileName = findViewById(R.id.tvFileName)
        tvFileSize = findViewById(R.id.tvFileSize)
        tvHashResult = findViewById(R.id.tvHashResult)
        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnCopyHash = findViewById(R.id.btnCopyHash)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
    }

    private fun setupListeners() {
        btnSelectFile.setOnClickListener {
            if (hasStoragePermission()) {
                openFilePicker()
            } else {
                requestStoragePermission()
            }
        }

        btnCopyHash.setOnClickListener {
            copyHashToClipboard()
        }
    }

    private fun checkAndRequestPermissions() {
        if (!hasStoragePermission()) {
            requestStoragePermission()
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6 to 12
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestStoragePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        ActivityCompat.requestPermissions(this, permissions, REQUEST_STORAGE_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Storage permission is required to select and verify video files. Please grant permission in Settings.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "video/*",
                "application/octet-stream",
                "*/*"
            ))
        }
        startActivityForResult(Intent.createChooser(intent, "Select Video File"), REQUEST_FILE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_FILE_PICKER && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                displayFileInfo(uri)
                generateHash(uri)
            }
        }
    }

    private fun displayFileInfo(uri: Uri) {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                
                val fileName = if (nameIndex >= 0) it.getString(nameIndex) else "Unknown"
                val fileSize = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L
                
                tvFileName.text = "File: $fileName"
                tvFileSize.text = "Size: ${formatFileSize(fileSize)}"
            }
        }
    }

    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$size bytes"
        }
    }

    private fun generateHash(uri: Uri) {
        coroutineScope.launch {
            try {
                // Show progress
                progressBar.visibility = View.VISIBLE
                tvStatus.text = "Calculating hash..."
                tvStatus.visibility = View.VISIBLE
                btnCopyHash.isEnabled = false
                tvHashResult.text = ""
                
                // Calculate hash in background
                val hash = withContext(Dispatchers.IO) {
                    calculateSHA256(uri)
                }
                
                // Update UI
                currentHash = hash
                tvHashResult.text = "SHA-256:\n$hash"
                tvStatus.text = "Hash generated successfully"
                btnCopyHash.isEnabled = true
                progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                // Handle error
                progressBar.visibility = View.GONE
                tvStatus.text = "Error: ${e.message}"
                tvHashResult.text = ""
                btnCopyHash.isEnabled = false
                Toast.makeText(this@HashGeneratorActivity, "Error generating hash: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun calculateSHA256(uri: Uri): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        
        contentResolver.openInputStream(uri)?.use { inputStream ->
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun copyHashToClipboard() {
        currentHash?.let { hash ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Video Hash", hash)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Hash copied to clipboard", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(this, "No hash to copy", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
