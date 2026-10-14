package com.charlotte.voice

/** Observable operating state, not a claim of consciousness. */
enum class CharlotteComponent { SPEECH, CONNECTIVITY, CONFIGURATION }
enum class HealthStatus { HEALTHY, DEGRADED, FAILED, UNKNOWN }
enum class RepairAction { NONE, RETRY_CONNECTION, RESTART_SPEECH, RESTORE_VERIFIED_CONFIGURATION, ESCALATE }

data class ComponentObservation(
    val component: CharlotteComponent,
    val status: HealthStatus,
    val observedAtMillis: Long,
    val detail: String = ""
)

data class RepairDecision(
    val action: RepairAction,
    val requiresApproval: Boolean,
    val explanation: String
)

/**
 * Safe policy engine. Does not execute shell commands, change permissions,
 * erase data, deploy patches, or claim that a repair succeeded.
 */
class CharlotteRecoveryEngine(private val staleAfterMillis: Long = 120_000L) {
    private val observations = mutableMapOf<CharlotteComponent, ComponentObservation>()
    private val attempted = mutableSetOf<CharlotteComponent>()

    fun observe(observation: ComponentObservation) {
        observations[observation.component] = observation
        if (observation.status == HealthStatus.HEALTHY) attempted.remove(observation.component)
    }

    fun snapshot(nowMillis: Long): Map<CharlotteComponent, HealthStatus> =
        CharlotteComponent.entries.associateWith { component ->
            val o = observations[component]
            if (o == null || nowMillis < o.observedAtMillis ||
                nowMillis - o.observedAtMillis > staleAfterMillis) HealthStatus.UNKNOWN
            else o.status
        }

    fun decide(component: CharlotteComponent, nowMillis: Long): RepairDecision {
        val state = snapshot(nowMillis)[component]
        if (state == HealthStatus.HEALTHY) return RepairDecision(RepairAction.NONE, false, "Component healthy")
        if (state == HealthStatus.UNKNOWN) return RepairDecision(RepairAction.ESCALATE, false, "Insufficient current diagnostic evidence")
        if (component in attempted) return RepairDecision(RepairAction.ESCALATE, false, "Repair already attempted; human review required")
        return when (component) {
            CharlotteComponent.CONNECTIVITY -> RepairDecision(RepairAction.RETRY_CONNECTION, false, "One bounded connection retry is allowed")
            CharlotteComponent.SPEECH -> RepairDecision(RepairAction.RESTART_SPEECH, false, "Restart only the app-owned speech session")
            CharlotteComponent.CONFIGURATION -> RepairDecision(RepairAction.RESTORE_VERIFIED_CONFIGURATION, true, "Configuration restoration requires approval and a verified backup")
        }
    }

    /** Record an attempt before execution; a subsequent health observation verifies outcome. */
    fun recordAttempt(component: CharlotteComponent, decision: RepairDecision) {
        require(decision.action != RepairAction.NONE && decision.action != RepairAction.ESCALATE)
        require(component !in attempted) { "Repeated automatic repair is blocked" }
        attempted.add(component)
    }
}
