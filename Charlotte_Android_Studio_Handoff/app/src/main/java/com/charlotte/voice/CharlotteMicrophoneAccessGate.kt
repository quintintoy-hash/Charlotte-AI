package com.charlotte.voice

/**
 * Permission and lifecycle gate for Charlotte microphone capture.
 * Pure Kotlin to keep permission-denial behavior independently testable.
 * This gate does not grant Android permissions or authenticate the account.
 */
internal object CharlotteMicrophoneAccessGate {
    enum class Decision { ALLOW, DENY_PERMISSION, DENY_UNSOLICITED, DENY_INACTIVE }

    fun start(microphoneGranted: Boolean, explicitStart: Boolean): Decision = when {
        !microphoneGranted -> Decision.DENY_PERMISSION
        !explicitStart -> Decision.DENY_UNSOLICITED
        else -> Decision.ALLOW
    }

    fun resume(microphoneGranted: Boolean, sessionExists: Boolean, paused: Boolean): Decision = when {
        !microphoneGranted -> Decision.DENY_PERMISSION
        !sessionExists || !paused -> Decision.DENY_INACTIVE
        else -> Decision.ALLOW
    }
}
