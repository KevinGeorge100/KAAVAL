package com.kaaval.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

/**
 * KAAVAL SMS Reply Listener
 * Automatically detects when a caregiver replies to an SOS alert.
 * This "closes the loop" without needing a backend server.
 */
class SmsReplyReceiver(
    private val emergencyNumbers: List<String>,
    private val onCaregiverAcknowledged: (senderName: String) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: ""
                val body = sms.displayMessageBody?.lowercase() ?: ""

                // Clean the sender number to match (handle +91, spaces etc)
                val isEmergencyContact = emergencyNumbers.any { 
                    sender.contains(it.takeLast(10)) 
                }

                if (isEmergencyContact) {
                    if (body.contains("ok") || body.contains("coming") || body.contains("help") || body.contains("on my way")) {
                        Log.i("SmsReplyReceiver", "Caregiver Acknowledgment Received via SMS from $sender")
                        onCaregiverAcknowledged(sender)
                        break
                    }
                }
            }
        }
    }
}
