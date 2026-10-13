package com.charlotte.voice

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WakeWordMatcherTest {
    @Test
    fun matchesWakeWordRegardlessOfCase() {
        assertTrue(WakeWordMatcher.containsWakeWord("Charlotte"))
        assertTrue(WakeWordMatcher.containsWakeWord("hello CHARLOTTE, are you there?"))
    }

    @Test
    fun doesNotMatchInsideAnotherWord() {
        assertFalse(WakeWordMatcher.containsWakeWord("charlottesville"))
        assertFalse(WakeWordMatcher.containsWakeWord("mycharlotte"))
    }

    @Test
    fun doesNotActivateForUnrelatedSpeech() {
        assertFalse(WakeWordMatcher.containsWakeWord("please turn on the lights"))
        assertFalse(WakeWordMatcher.containsWakeWord(""))
    }
}
