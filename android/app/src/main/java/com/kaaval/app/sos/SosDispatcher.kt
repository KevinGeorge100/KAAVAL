package com.kaaval.app.sos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.net.toUri
import com.kaaval.app.domain.model.EmergencyContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SosDispatcher(private val context: Context) {

    // KAAVAL Telegram Bot Configuration (Free & Unlimited)
    private val BOT_TOKEN = "PASTE_TELEGRAM_BOT_TOKEN"
    private val CHAT_ID = "PASTE_TELEGRAM_CHAT_ID"

    suspend fun dispatchEmergencyAlert(
        contacts: List<EmergencyContact>,
        latitude: Double?,
        longitude: Double?,
        trackingUrl: String,
        medicalNotes: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        
        if (contacts.isEmpty()) {
            Log.w("SosDispatcher", "No emergency contacts configured")
            return@withContext false
        }

        val googleMapsUrl = if (latitude != null && longitude != null) 
            "https://maps.google.com/?q=$latitude,$longitude" else "Location unavailable"

        val alertText = """
            🚨 *KAAVAL EMERGENCY ALERT* 🚨
            
            *Student Name*: Visually Impaired User
            *Location*: $googleMapsUrl
            *Medical Info*: $medicalNotes
            *Live Tracking*: $trackingUrl
            
            _Help is needed immediately!_
        """.trimIndent()

        // 1. Send FREE Telegram Alert (Primary)
        sendTelegramAlert(alertText)

        // 2. Send SMS fallback (Secondary/Carrier Charges)
        val smsMessage = "🚨 EMERGENCY SOS ALERT from KAAVAL 🚨\nLocation: $googleMapsUrl\nTracking: $trackingUrl"
        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        for (contact in contacts) {
            try {
                smsManager.sendTextMessage(contact.phoneNumber, null, smsMessage, null, null)
                Log.d("SosDispatcher", "SMS fallback sent to ${contact.name}")
            } catch (e: Exception) {
                Log.e("SosDispatcher", "SMS failed: ${e.message}")
            }
        }

        // 3. Auto-call primary contact
        val primaryContact = contacts.find { it.isPrimary } ?: contacts.firstOrNull()
        if (primaryContact != null) {
            withContext(Dispatchers.Main) {
                initiateCall(primaryContact.phoneNumber)
            }
        }

        return@withContext true
    }

    private fun sendTelegramAlert(message: String) {
        if (BOT_TOKEN.startsWith("PASTE")) {
            Log.w("SosDispatcher", "Telegram Bot not configured. Skipping free alert.")
            return
        }

        try {
            val encodedMsg = URLEncoder.encode(message, "UTF-8")
            val url = URL("https://api.telegram.org/bot$BOT_TOKEN/sendMessage?chat_id=$CHAT_ID&text=$encodedMsg&parse_mode=Markdown")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            if (conn.responseCode == 200) {
                Log.i("SosDispatcher", "Telegram Alert Sent Successfully (Free)")
            } else {
                Log.e("SosDispatcher", "Telegram API Error: ${conn.responseCode}")
            }
        } catch (e: Exception) {
            Log.e("SosDispatcher", "Telegram Dispatch Failed: ${e.message}")
        }
    }

    fun dispatchSafeStatus(contacts: List<EmergencyContact>, message: String) {
        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        for (contact in contacts) {
            try {
                smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
            } catch (e: Exception) {
                Log.e("SosDispatcher", "Failed to send safe SMS to ${contact.phoneNumber}", e)
            }
        }
    }

    private fun initiateCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = "tel:$phoneNumber".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SosDispatcher", "Direct ACTION_CALL failed, falling back to ACTION_DIAL", e)
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:$phoneNumber".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        }
    }
}
