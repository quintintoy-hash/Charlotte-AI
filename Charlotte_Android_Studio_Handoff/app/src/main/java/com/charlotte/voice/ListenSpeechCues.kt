package com.charlotte.voice

import java.util.Locale

/**
 * Conservative text-only helper for an explicitly authorized Listen session.
 * Does not detect lies, infer emotions from voice, or record audio.
 */
object ListenSpeechCues {
    data class Observation(val kind: Kind, val evidence: String, val note: String)
    enum class Kind { EXPLICIT_SELF_REPORT, POSSIBLE_TEXT_CONTRADICTION }

    /**
     * Opt-in defaults to false. No processing occurs unless consent was established.
     * No output is a claim about truthfulness, intent or psychological state.
     */
    fun analyze(transcript: String, consented: Boolean = false): List<Observation> {
        if (!consented || transcript.isBlank()) return emptyList()
        val text = transcript.lowercase(Locale.ROOT)
        val reports = listOf("i am scared", "i'm scared", "i feel scared",
            "i am afraid", "i'm afraid", "i feel afraid",
            "i am nervous", "i'm nervous", "i feel nervous",
            "i am stressed", "i'm stressed", "i feel stressed")
        return reports.filter { phrase ->
            Regex("(?<![\\p{L}])" + Regex.escape(phrase) + "(?![\\p{L}])")
                .containsMatchIn(text)
        }.map { phrase ->
            Observation(Kind.EXPLICIT_SELF_REPORT, phrase,
                "Speaker used these words; this does not independently establish emotion or danger.")
        }
    }
}
