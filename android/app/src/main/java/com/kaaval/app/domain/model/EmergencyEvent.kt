package com.kaaval.app.domain.model

/**
 * KAAVAL Emergency Events
 * These events describe what happened in the system to trigger a state transition.
 */
sealed class EmergencyEvent {
    object ButtonPressed : EmergencyEvent()
    
    data class ButtonHolding(val progress: Float) : EmergencyEvent()
    
    object ButtonReleased : EmergencyEvent()

    object InstantSosTriggered : EmergencyEvent()
    
    data class CountdownTicked(val secondsRemaining: Int) : EmergencyEvent()
    
    object CountdownFinished : EmergencyEvent()
    
    object CountdownCancelled : EmergencyEvent()
    
    object EmergencyActivated : EmergencyEvent()
    
    object AcquisitionStarted : EmergencyEvent()
    
    data class LocationAcquired(val latitude: Double, val longitude: Double) : EmergencyEvent()
    
    object LocationFailed : EmergencyEvent()
    
    object AlertsSent : EmergencyEvent()
    
    object PrimaryContactCallStarted : EmergencyEvent()
    
    data class LiveTrackingStarted(val latitude: Double? = null, val longitude: Double? = null) : EmergencyEvent()
    
    object EmergencyCompleted : EmergencyEvent()
    
    data class CallTicked(val durationSeconds: Int) : EmergencyEvent()
    
    data class TrackingTicked(val elapsedTimeSeconds: Int) : EmergencyEvent()
    
    data class ErrorOccurred(val reason: String) : EmergencyEvent()
    
    data class CriticalBatteryDetected(val level: Int) : EmergencyEvent()
    
    data class SessionRecovered(
        val incidentId: String,
        val latitude: Double?,
        val longitude: Double?
    ) : EmergencyEvent()
    
    object ResetToIdle : EmergencyEvent()
}
