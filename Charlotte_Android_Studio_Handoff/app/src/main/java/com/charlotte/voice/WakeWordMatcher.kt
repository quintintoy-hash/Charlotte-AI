package com.charlotte.voice

import java.util.Locale

/**
 * Matches the explicitly configured wake word as a complete spoken word.
 * This is text matching only; it does not capture microphone audio.
 */
object WakeWordMatcher {
    private val wakeWord = Regex("(?<![\\p{L}])charlotte(?![\\p{L}])", RegexOption.IGNORE_CASE)

    fun containsWakeWord(transcript: String): Boolean =
        wakeWord.containsMatchIn(transcript.lowercase(Locale.ROOT))
}
