package com.charlotte.voice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class CustomGreetingStoreTest {
    @Test fun onlyVerifiedUserGetsGreeting() {
        val store = CustomGreetingStore()
        store.set("boss", GreetingEvent.MORNING, "Morning, Boss")
        assertNull(store.getForVerifiedUser("boss", GreetingEvent.MORNING, false))
        assertNull(store.getForVerifiedUser("someone-else", GreetingEvent.MORNING, true))
        assertEquals("Morning, Boss", store.getForVerifiedUser("boss", GreetingEvent.MORNING, true))
    }

    @Test fun linkMessageRequiresCompletedPairing() {
        val store = CustomGreetingStore()
        store.set("boss", GreetingEvent.LINK_TO_DEVICE, "Connected, Boss")
        assertNull(store.linkedDeviceMessage("boss", true, false))
        assertNull(store.linkedDeviceMessage("boss", false, true))
        assertEquals("Connected, Boss", store.linkedDeviceMessage("boss", true, true))
    }

    @Test fun userCanReplaceAndDeleteOwnGreeting() {
        val store = CustomGreetingStore()
        store.set("boss", GreetingEvent.EVENING, "Hello")
        store.set("boss", GreetingEvent.EVENING, "Good evening")
        assertEquals("Good evening", store.getForVerifiedUser("boss", GreetingEvent.EVENING, true))
        store.remove("boss", GreetingEvent.EVENING)
        assertNull(store.getForVerifiedUser("boss", GreetingEvent.EVENING, true))
    }

    @Test fun emptyGreetingIsRejected() {
        val store = CustomGreetingStore()
        assertFailsWith<IllegalArgumentException> { store.set("boss", GreetingEvent.STARTUP, "  ") }
    }
}
