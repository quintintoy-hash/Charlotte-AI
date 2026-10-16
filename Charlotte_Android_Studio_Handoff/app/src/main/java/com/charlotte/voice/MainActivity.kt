package com.charlotte.voice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.os.Build
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView
import android.media.AudioManager
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * User-initiated speech transcription prototype. Does not listen in the background.
 * Android's installed speech provider may process audio online.
 */
class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var transcript: TextView
    private lateinit var listenButton: Button
    private var recognizer: SpeechRecognizer? = null
    private var listening = false
    private var pendingVoiceSession = false
    private var speaker: TextToSpeech? = null
    private var speechReady = false
    private var ttsInitialized = false
    private var pendingSpeech: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speaker = TextToSpeech(this) { result ->
            runOnUiThread {
                ttsInitialized = true
                speechReady = result == TextToSpeech.SUCCESS
                if (speechReady) {
                    val languageResult = speaker?.setLanguage(Locale.UK)
                    speechReady = languageResult != TextToSpeech.LANG_MISSING_DATA &&
                        languageResult != TextToSpeech.LANG_NOT_SUPPORTED
                    speaker?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            runOnUiThread { status.text = "Charlotte speaking..." }
                        }
                        override fun onDone(utteranceId: String?) {
                            runOnUiThread { status.text = "Charlotte finished speaking." }
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            runOnUiThread { status.text = "Voice playback error. Check phone media volume and TTS settings." }
                        }
                        override fun onError(utteranceId: String?, errorCode: Int) {
                            runOnUiThread { status.text = "Voice playback error: $errorCode" }
                        }
                    })
                }
                val queued = pendingSpeech
                pendingSpeech = null
                if (queued != null) speakReply(queued)
                else if (!speechReady) status.text = "Speech engine unavailable. Check installed text-to-speech voices."
            }
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }
        layout.addView(TextView(this).apply {
            text = "Charlotte"
            textSize = 28f
        })
        status = TextView(this).apply {
            text = "Ready. Microphone inactive."
            textSize = 16f
        }
        transcript = TextView(this).apply {
            text = "Tap Listen to transcribe a short spoken phrase."
            textSize = 18f
        }
        listenButton = Button(this).apply {
            text = "Listen"
            setOnClickListener {
                if (listening) stopListening() else requestToListen()
            }
        }
        layout.addView(status)
        volumeControlStream = AudioManager.STREAM_MUSIC
        layout.addView(Button(this).apply {
            text = "TEST CHARLOTTE VOICE"
            setOnClickListener { speakReply("Hello, Boss. Charlotte voice test successful.") }
        })
        layout.addView(transcript)
        layout.addView(listenButton)
        layout.addView(Button(this).apply {
            text = "Start voice session"
            setOnClickListener {
                if (listening) stopListening()
                requestVoiceSession()
            }
        })
        layout.addView(Button(this).apply {
            text = "Stop voice session"
            setOnClickListener {
                stopService(Intent(this@MainActivity, CharlotteVoiceForegroundService::class.java))
                status.text = "Voice session stop requested."
            }
        })
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun requestVoiceSession() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition unavailable."
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            pendingVoiceSession = true
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
            return
        }
        startVoiceSession()
    }

    private fun startVoiceSession() {
        pendingVoiceSession = false
        if (listening) stopListening()
        try {
            val intent = Intent(this, CharlotteVoiceForegroundService::class.java).apply {
                action = CharlotteVoiceForegroundService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
            status.text = "Voice session requested. Use notification Stop to end."
        } catch (e: Exception) {
            status.text = "Voice session could not start. Check microphone permissions."
        }
    }

    private fun requestToListen() {
        stopService(Intent(this, CharlotteVoiceForegroundService::class.java))
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition is unavailable on this device."
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
            return
        }
        startListening()
    }

    private fun startListening() {
        if (listening) return
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        status.text = "Listening. Tap Stop to finish."
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        status.text = "Processing speech..."
                    }
                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognised. Tap Listen and speak clearly."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard. Try again after the listening prompt."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Allow microphone access in Settings."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network speech service unavailable. Retry."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy. Wait and retry."
                            else -> "Speech recognition error $error. Retry."
                        }
                        finishListening(message)
                    }
                    override fun onResults(results: Bundle?) {
                        val phrases = results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )
                        val spoken = phrases?.firstOrNull() ?: ""
                        transcript.text = spoken.ifBlank { "No words recognised." }
                        val wakeDetected = WakeWordMatcher.containsWakeWord(spoken)
                        finishListening(if (wakeDetected) {
                            "Charlotte detected. Microphone inactive."
                        } else {
                            "Microphone inactive. Say Charlotte to activate."
                        })
                        if (wakeDetected) {
                            val reply = when {
                                spoken.contains("hear me", ignoreCase = true) ->
                                    "Loud and clear, Boss. What shall we get up to today?"
                                spoken.contains("hello", ignoreCase = true) ->
                                    "Hello, Boss. Lovely to hear from you."
                                else -> "I'm listening, Boss. My full conversational brain isn't connected yet."
                            }
                            status.text = reply
                            speakReply(reply)
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-GB")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        listening = true
        listenButton.text = "Stop"
        status.text = "Starting speech recognition..."
        try {
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            finishListening("Unable to start speech recognition.")
        }
    }

    private fun speakReply(reply: String) {
        if (!ttsInitialized) {
            pendingSpeech = reply
            status.text = "Preparing Charlotte voice..."
            return
        }
        if (!speechReady) {
            status.text = "Charlotte speech engine unavailable. Check phone TTS voice data."
            return
        }
        val result = speaker?.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "charlotte-reply")
        status.text = if (result == TextToSpeech.SUCCESS) reply else "Charlotte speech playback failed."
    }

    private fun finishListening(message: String) {
        listening = false
        recognizer?.destroy()
        recognizer = null
        listenButton.text = "Listen"
        status.text = message
    }

    private fun stopListening() {
        recognizer?.cancel()
        finishListening("Stopped. Microphone inactive.")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_MICROPHONE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                if (pendingVoiceSession) startVoiceSession() else startListening()
            } else {
                pendingVoiceSession = false
                status.text = "Microphone permission denied. Microphone inactive."
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (listening) stopListening()
    }

    override fun onDestroy() {
        recognizer?.destroy()
        recognizer = null
        speaker?.stop()
        speaker?.shutdown()
        speaker = null
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_MICROPHONE = 1001
    }
}
