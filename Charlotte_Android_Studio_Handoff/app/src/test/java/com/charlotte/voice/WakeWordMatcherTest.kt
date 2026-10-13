package com.charlotte.voice

import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

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
