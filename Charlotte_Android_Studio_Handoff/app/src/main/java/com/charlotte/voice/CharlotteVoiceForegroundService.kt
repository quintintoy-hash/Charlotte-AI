package com.charlotte.voice

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Explicitly started, visible speech-recognition session.
 * The system speech provider may transmit audio to a network service.
 * This is best-effort recognition, not guaranteed always-on wake-word detection.
 */
class CharlotteVoiceForegroundService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var active = false
    private var speaker: TextToSpeech? = null
    private var speechReady = false
    private var attempts = 0
    private var sessionGeneration = 0
    private val retry = Runnable { listen() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action != ACTION_START ||
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            !SpeechRecognizer.isRecognitionAvailable(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (active) return START_NOT_STICKY
        active = true
        speaker = TextToSpeech(this) { code ->
            speechReady = code == TextToSpeech.SUCCESS
            if (speechReady) {
                val language = speaker?.setLanguage(Locale.UK)
                speechReady = language != TextToSpeech.LANG_MISSING_DATA &&
                    language != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
        sessionGeneration++
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Charlotte voice session", NotificationManager.IMPORTANCE_LOW)
        )
        showNotification("Listening session starting. Speech provider may use the internet.")
        listen()
        return START_NOT_STICKY
    }

    private fun showNotification(message: String) {
        val stopIntent = Intent(this, CharlotteVoiceForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stop = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Charlotte voice session")
            .setContentText(message)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(
                android.R.drawable.ic_media_pause, "Stop microphone", stop
            ).build())
            .build()
        startForeground(101, notification)
    }

    private fun listen() {
        if (!active) return
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }
        recognizer?.destroy()
        recognizer = null
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            stopSelf()
            return
        }
        val generation = sessionGeneration
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (!active || generation != sessionGeneration) return
                    showNotification("Microphone active. Say Charlotte. Tap Stop to end.")
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    if (!active || generation != sessionGeneration) return
                    showNotification("Processing speech. Tap Stop to end.")
                }
                override fun onError(error: Int) {
                    if (!active || generation != sessionGeneration) return
                    if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ||
                        error == SpeechRecognizer.ERROR_CLIENT) {
                        stopSelf()
                    } else {
                        scheduleNext()
                    }
                }
                override fun onResults(results: Bundle?) {
                    if (!active || generation != sessionGeneration) return
                    val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull().orEmpty()
                    if (spoken.isNotBlank()) attempts = 0
                    if (WakeWordMatcher.containsWakeWord(spoken)) {
                        val reply = when {
                            spoken.contains("hear me", ignoreCase = true) -> "Loud and clear, Boss."
                            spoken.contains("hello", ignoreCase = true) -> "Hello, Boss. Lovely to hear from you."
                            else -> "I'm here, Boss."
                        }
                        if (speechReady) {
                            speaker?.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "charlotte-session-reply")
                            showNotification("Charlotte responding: $reply")
                        } else {
                            showNotification("Charlotte detected. Speech engine not ready.")
                        }
                    }
                    scheduleNext()
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-GB")
        }
        try {
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun scheduleNext() {
        if (!active) return
        recognizer?.destroy()
        recognizer = null
        sessionGeneration++
        attempts++
        if (attempts > 8) {
            stopSelf()
            return
        }
        handler.removeCallbacks(retry)
        handler.postDelayed(retry, (500L * attempts).coerceAtMost(4000L))
    }

    override fun onDestroy() {
        active = false
        sessionGeneration++
        handler.removeCallbacks(retry)
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        speaker?.stop()
        speaker?.shutdown()
        speaker = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.charlotte.voice.START_SESSION"
        const val ACTION_STOP = "com.charlotte.voice.STOP_SESSION"
        private const val CHANNEL = "charlotte_voice_session"
    }
}
