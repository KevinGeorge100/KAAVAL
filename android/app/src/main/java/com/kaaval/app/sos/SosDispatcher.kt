package com.kaaval.app.sos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import com.kaaval.app.domain.model.EmergencyContact

class SosDispatcher(private val context: Context) {

    fun dispatchEmergencyAlert(
        contacts: List<EmergencyContact>,
        latitude: Double?,
        longitude: Double?,
        trackingUrl: String
    ): Boolean {
        if (contacts.isEmpty()) {
            Log.w("SosDispatcher", "No emergency contacts configured")
            return false
        }

        val locationText = if (latitude != null && longitude != null) {
            "Live GPS: https://maps.google.com/?q=$latitude,$longitude\nTracking Portal: $trackingUrl"
        } else {
            "Location currently obtaining... Tracking Portal: $trackingUrl"
        }

        val smsMessage = "🚨 EMERGENCY SOS ALERT from KAAVAL 🚨\nI need immediate assistance!\n$locationText"

        val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)

        for (contact in contacts) {
            try {
                smsManager.sendTextMessage(
                    contact.phoneNumber,
                    null,
                    smsMessage,
                    null,
                    null
                )
                Log.d("SosDispatcher", "SMS sent to ${contact.name} (${contact.phoneNumber})")
            } catch (e: Exception) {
                Log.e("SosDispatcher", "Failed to send SMS to ${contact.phoneNumber}", e)
            }
        }

        // Auto-call primary contact
        val primaryContact = contacts.find { it.isPrimary } ?: contacts.firstOrNull()
        if (primaryContact != null) {
            initiateCall(primaryContact.phoneNumber)
        }

        return true
    }

    private fun initiateCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SosDispatcher", "Direct ACTION_CALL failed, falling back to ACTION_DIAL", e)
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        }
    }
}
