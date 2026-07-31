package com.kaaval.app.domain.model

sealed class EmergencyState {
    object Idle : EmergencyState()
    data class Countdown(val secondsRemaining: Int) : EmergencyState()
    data class Active(
        val incidentId: String,
        val timestamp: Long,
        val latitude: Double?,
        val longitude: Double?,
        val trackingUrl: String,
        val isPrimaryCalled: Boolean = false,
        val respondingCaregiver: String? = null // New: To track who is helping
    ) : EmergencyState()
    object Cancelled : EmergencyState()
    object Resolved : EmergencyState()
}
