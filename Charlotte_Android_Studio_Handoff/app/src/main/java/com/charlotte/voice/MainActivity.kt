package com.charlotte.voice

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Minimal launchable Charlotte shell. No microphone recording is started here.
 */
class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }
        val title = TextView(this).apply {
            text = "Charlotte"
            textSize = 28f
        }
        status = TextView(this).apply {
            text = "Ready. Microphone inactive."
            textSize = 16f
        }
        val permissionButton = Button(this).apply {
            text = "Request microphone permission"
            setOnClickListener {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {
                    status.text = "Permission granted. Microphone inactive."
                } else {
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
                }
            }
        }
        layout.addView(title)
        layout.addView(status)
        layout.addView(permissionButton)
        setContentView(layout)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_MICROPHONE) {
            val granted = grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            status.text = if (granted) {
                "Permission granted. Microphone inactive."
            } else {
                "Microphone permission denied. Microphone inactive."
            }
        }
    }

    companion object {
        private const val REQUEST_MICROPHONE = 1001
    }
}
