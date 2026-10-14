package com.charlotte.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ListenSpeechCuesTest {
    @Test fun disabledByDefault() {
        assertTrue(ListenSpeechCues.analyze("I'm scared").isEmpty())
    }
    @Test fun explicitSelfReportIsNotLieDetection() {
        val result = ListenSpeechCues.analyze("I'm scared, please help", consented = true)
        assertEquals(1, result.size)
        assertEquals(ListenSpeechCues.Kind.EXPLICIT_SELF_REPORT, result.first().kind)
        assertTrue(result.first().note.contains("does not independently establish"))
    }
    @Test fun nervousVoiceCannotBeInferredFromText() {
        assertTrue(ListenSpeechCues.analyze("Well, maybe, um...", consented = true).isEmpty())
    }
    @Test fun avoidsSubstringMatches() {
        assertTrue(ListenSpeechCues.analyze("I am scaredy", consented = true).isEmpty())
    }
}
