package com.charlotte.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Visible foreground-session scaffold. Does not record audio or claim wake-word detection.
 * Audio capture must be implemented separately with permission and lifecycle controls.
 */
class CharlotteVoiceForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != ACTION_START) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Charlotte voice session", NotificationManager.IMPORTANCE_LOW)
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Charlotte voice session")
            .setContentText("Session scaffold active; microphone recording is not enabled.")
            .build()
        startForeground(101, notification)
        return START_NOT_STICKY
    }

    companion object {
        const val ACTION_START = "com.charlotte.voice.START_SESSION"
        private const val CHANNEL = "charlotte_voice_session"
    }
}
