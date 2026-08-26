package com.kaaval.app.domain.model

sealed class EmergencyState {
    object Idle : EmergencyState()
    
    data class HoldingButton(val progress: Float) : EmergencyState()
    
    data class Countdown(val secondsRemaining: Int) : EmergencyState()
    
    object Activated : EmergencyState()
    
    object AcquiringLocation : EmergencyState()
    
    data class LocationReady(val latitude: Double, val longitude: Double) : EmergencyState()
    
    object SendingAlerts : EmergencyState()
    
    data class CallingPrimaryContact(val durationSeconds: Int) : EmergencyState()
    
    data class LiveTracking(
        val incidentId: String,
        val timestamp: Long,
        val elapsedTimeSeconds: Int = 0,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val trackingUrl: String = "",
        val isPrimaryCalled: Boolean = false,
        val respondingCaregiver: String? = null
    ) : EmergencyState()
    
    object Completed : EmergencyState()
    
    data class Error(val reason: String) : EmergencyState()
    
    object Cancelled : EmergencyState()
    
    object Resolved : EmergencyState()
}
