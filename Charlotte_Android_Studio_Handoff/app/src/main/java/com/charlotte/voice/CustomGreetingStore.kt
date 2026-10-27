package com.charlotte.voice

/**
 * Per-user greeting selection. Storage, identity verification and device pairing
 * are deliberately separate responsibilities; this class grants no access.
 */
enum class GreetingEvent { MORNING, AFTERNOON, EVENING, STARTUP, DEVICE_CONNECTION, LINK_TO_DEVICE, RETURN_SESSION }

class CustomGreetingStore {
    private val greetings = mutableMapOf<String, MutableMap<GreetingEvent, String>>()

    fun set(userId: String, event: GreetingEvent, message: String) {
        require(userId.isNotBlank()) { "User identity is required" }
        require(message.isNotBlank() && message.length <= 500) { "Greeting must contain 1–500 characters" }
        greetings.getOrPut(userId) { mutableMapOf() }[event] = message.trim()
    }

    fun remove(userId: String, event: GreetingEvent) {
        greetings[userId]?.remove(event)
    }

    /** Call only after the user has been identified and access authorized. */
    fun getForVerifiedUser(userId: String, event: GreetingEvent, verified: Boolean): String? {
        if (!verified || userId.isBlank()) return null
        return greetings[userId]?.get(event)
    }

    /** Device-link message must never play before pairing completes. */
    fun linkedDeviceMessage(userId: String, verified: Boolean, pairingCompleted: Boolean): String? {
        if (!pairingCompleted) return null
        return getForVerifiedUser(userId, GreetingEvent.LINK_TO_DEVICE, verified)
    }
}
