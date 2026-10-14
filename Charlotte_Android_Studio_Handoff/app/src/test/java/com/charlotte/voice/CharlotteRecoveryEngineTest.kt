package com.charlotte.voice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class CharlotteRecoveryEngineTest {
    @Test fun unknownOrStaleStateCannotTriggerRepair() {
        val engine = CharlotteRecoveryEngine(100L)
        assertEquals(RepairAction.ESCALATE, engine.decide(CharlotteComponent.SPEECH, 10L).action)
        engine.observe(ComponentObservation(CharlotteComponent.SPEECH, HealthStatus.FAILED, 10L))
        assertEquals(RepairAction.ESCALATE, engine.decide(CharlotteComponent.SPEECH, 111L).action)
    }

    @Test fun failedSpeechAllowsOneBoundedRestart() {
        val engine = CharlotteRecoveryEngine()
        engine.observe(ComponentObservation(CharlotteComponent.SPEECH, HealthStatus.FAILED, 10L))
        val decision = engine.decide(CharlotteComponent.SPEECH, 11L)
        assertEquals(RepairAction.RESTART_SPEECH, decision.action)
        assertFalse(decision.requiresApproval)
        engine.recordAttempt(CharlotteComponent.SPEECH, decision)
        assertEquals(RepairAction.ESCALATE, engine.decide(CharlotteComponent.SPEECH, 12L).action)
        assertFailsWith<IllegalArgumentException> { engine.recordAttempt(CharlotteComponent.SPEECH, decision) }
    }

    @Test fun configurationRestoreRequiresApproval() {
        val engine = CharlotteRecoveryEngine()
        engine.observe(ComponentObservation(CharlotteComponent.CONFIGURATION, HealthStatus.DEGRADED, 10L))
        val decision = engine.decide(CharlotteComponent.CONFIGURATION, 11L)
        assertEquals(RepairAction.RESTORE_VERIFIED_CONFIGURATION, decision.action)
        assertTrue(decision.requiresApproval)
    }

    @Test fun healthyObservationResetsAttemptState() {
        val engine = CharlotteRecoveryEngine()
        engine.observe(ComponentObservation(CharlotteComponent.CONNECTIVITY, HealthStatus.FAILED, 10L))
        engine.recordAttempt(CharlotteComponent.CONNECTIVITY, engine.decide(CharlotteComponent.CONNECTIVITY, 11L))
        engine.observe(ComponentObservation(CharlotteComponent.CONNECTIVITY, HealthStatus.HEALTHY, 12L))
        assertEquals(RepairAction.NONE, engine.decide(CharlotteComponent.CONNECTIVITY, 13L).action)
    }
}
