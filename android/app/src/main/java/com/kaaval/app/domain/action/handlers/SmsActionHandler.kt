package com.kaaval.app.domain.action.handlers

import android.util.Log
import com.kaaval.app.data.KaavalRepository
import com.kaaval.app.domain.model.EmergencyAction
import com.kaaval.app.domain.model.EmergencyEvent
import com.kaaval.app.domain.model.MedicalProfile
import com.kaaval.app.sos.SosDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * KAAVAL SMS Action Handler
 * Responsible for managing SMS alerts and safely bridging with the SosDispatcher.
 */
class SmsActionHandler(
    private val sosDispatcher: SosDispatcher,
    private val repository: KaavalRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun handle(action: EmergencyAction, onActionHandled: (EmergencyEvent) -> Unit) {
        if (action !is EmergencyAction.SendSmsAlerts) return

        Log.i("SmsHandler", "Dispatching Urgent SMS Alerts to Caregivers...")
        
        scope.launch {
            val contacts = repository.allContacts.first()
            val profile = repository.medicalProfile.first() ?: MedicalProfile(
                fullName = "Visually Impaired User",
                age = 0,
                bloodGroup = "Unknown",
                allergies = "Unknown",
                medications = "Unknown",
                emergencyNotes = "Visually impaired. Guided assistance required."
            )
            
            val incidentId = kotlinx.coroutines.withTimeoutOrNull(5000) {
                repository.getActiveSession().first { it != null }?.incidentId
            }
            val trackingUrl = incidentId?.let { "https://kaaval-94c1d.web.app/live/$it" }
                ?: "Live tracking unavailable"
            
            val isBatteryCritical = action.alertType == EmergencyAction.AlertType.BATTERY_CRITICAL
            val medicalNotes = if (isBatteryCritical) {
                "⚠️ FINAL SOS: Battery critical. Phone may shut down soon. " + profile.emergencyNotes
            } else {
                profile.emergencyNotes
            }

            val success = sosDispatcher.dispatchEmergencyAlert(
                contacts = contacts,
                latitude = action.latitude,
                longitude = action.longitude,
                trackingUrl = trackingUrl,
                medicalNotes = medicalNotes
            )
            
            if (success) {
                onActionHandled(EmergencyEvent.AlertsSent)
            }
        }
    }

    fun handleCall(onActionHandled: (EmergencyEvent) -> Unit) {
        scope.launch {
            val contacts = repository.allContacts.first()
            val primaryContact = contacts.find { it.isPrimary } ?: contacts.firstOrNull()
            
            if (primaryContact != null) {
                // High-priority announcement of the specific caregiver's name (Differentiator #5)
                com.kaaval.app.accessibility.VoiceFeedbackManager.speakCaregiverCall(primaryContact.name)

                // Add a delay to allow the "Calling..." announcement to finish before dialer opens
                delay(2000.milliseconds)

                sosDispatcher.initiateCall(primaryContact.phoneNumber)
                onActionHandled(EmergencyEvent.LiveTrackingStarted())
            }
        }
    }
}
