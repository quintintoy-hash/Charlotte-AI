package com.charlotte.voice

import org.junit.Test
import org.junit.Assert.assertEquals

class CharlotteMicrophoneAccessGateTest {
    @Test fun `start requires permission and explicit user action`() {
        val gate = CharlotteMicrophoneAccessGate
        assertEquals(CharlotteMicrophoneAccessGate.Decision.ALLOW, gate.start(true, true))
        assertEquals(CharlotteMicrophoneAccessGate.Decision.DENY_PERMISSION, gate.start(false, true))
        assertEquals(CharlotteMicrophoneAccessGate.Decision.DENY_UNSOLICITED, gate.start(true, false))
        assertEquals(CharlotteMicrophoneAccessGate.Decision.DENY_PERMISSION, gate.start(false, false))
    }

    @Test fun `resume requires permission active session and paused state`() {
        val gate = CharlotteMicrophoneAccessGate
        assertEquals(CharlotteMicrophoneAccessGate.Decision.ALLOW, gate.resume(true, true, true))
        assertEquals(CharlotteMicrophoneAccessGate.Decision.DENY_PERMISSION, gate.resume(false, true, true))
        assertEquals(CharlotteMicrophoneAccessGate.Decision.DENY_INACTIVE, gate.resume(true, false, true))
        assertEquals(CharlotteMicrophoneAccessGate.Decision.DENY_INACTIVE, gate.resume(true, true, false))
        assertEquals(CharlotteMicrophoneAccessGate.Decision.DENY_PERMISSION, gate.resume(false, false, false))
    }
}
