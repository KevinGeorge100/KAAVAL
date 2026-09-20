package com.kaaval.app.domain.model

/**
 * KAAVAL Emergency Actions
 * Commands that represent work to be executed by external services.
 * States and Events describe the "What", Actions describe the "Do".
 */
sealed class EmergencyAction {
    object StartCountdown : EmergencyAction()
    object CancelCountdown : EmergencyAction()
    object AcquireLocation : EmergencyAction()
    data class SendSmsAlerts(
        val latitude: Double?,
        val longitude: Double?,
        val alertType: AlertType = AlertType.STANDARD
    ) : EmergencyAction()
    
    enum class AlertType {
        STANDARD,
        BATTERY_CRITICAL
    }
    object CallPrimaryContact : EmergencyAction()
    object StartLiveTracking : EmergencyAction()
    object StopLiveTracking : EmergencyAction()
    
    data class PlayVoiceAnnouncement(val announcementType: String, val isPriority: Boolean = false) : EmergencyAction()
    data class PlayHapticPattern(val patternType: String) : EmergencyAction()
    
    data class StartAudioWitness(val incidentId: String) : EmergencyAction()
    object StopAudioWitness : EmergencyAction()
    
    object StartBatteryMonitoring : EmergencyAction()
    object StopBatteryMonitoring : EmergencyAction()
    
    data class UpdateLiveTracking(
        val latitude: Double?,
        val longitude: Double?,
        val trackingUrl: String
    ) : EmergencyAction()

    data class StartSession(val incidentId: String) : EmergencyAction()
    
    data class CompositeAction(val actions: List<EmergencyAction>) : EmergencyAction()
    object PersistIncident : EmergencyAction()
    object LogAnalytics : EmergencyAction()
    object CompleteEmergency : EmergencyAction()
    object ResetState : EmergencyAction()
}
