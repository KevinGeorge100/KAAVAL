package com.kaaval.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.KeyEvent

/**
 * KAAVAL Global Accessibility Service
 * Intercepts hardware key events (Volume UP x3) to trigger SOS from anywhere,
 * including the lock screen or when the app is in the background.
 */
class KaavalAccessibilityService : AccessibilityService() {

    private var volumeUpClickCount = 0
    private var lastVolumeUpTime = 0L

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        // Only listen for Volume Up clicks
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && action == KeyEvent.ACTION_DOWN) {
            val currentTime = System.currentTimeMillis()
            Log.d("KaavalAccessibility", "Volume UP Down detected. Count=${volumeUpClickCount + 1}")
            
            // Check timing between clicks (must be within 2 seconds for accessibility)
            if (currentTime - lastVolumeUpTime < 2000) {
                volumeUpClickCount++
            } else {
                volumeUpClickCount = 1
            }
            lastVolumeUpTime = currentTime

            if (volumeUpClickCount >= 3) {
                Log.i("KaavalAccessibility", "!!! TRIPLE CLICK DETECTED !!!")
                volumeUpClickCount = 0
                triggerGlobalSos()
                return true // Intercept the event so it doesn't change volume
            }
        }
        return false // Don't block other keys
    }

    private fun triggerGlobalSos() {
        Log.i("KaavalAccessibility", "!!! GLOBAL SOS DISPATCH INITIATED !!!")
        
        // 1. Force wake the screen and launch the UI
        val intent = Intent(this, com.kaaval.app.MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("EXTRA_TRIGGER_SOS", true)
        }
        
        try {
            startActivity(intent)
            Log.d("KaavalAccessibility", "MainActivity launch intent sent.")
        } catch (e: Exception) {
            Log.e("KaavalAccessibility", "Failed to launch MainActivity: ${e.message}")
        }

        // The Activity persists one incident before starting its foreground service.
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        // Not used for key interception
    }

    override fun onInterrupt() {
        // Not used
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i("KaavalAccessibility", "Accessibility Service Connected and Configured")
        
        // Ensure flag is set even if XML has issues on some OEMs
        val info = serviceInfo
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        serviceInfo = info
    }
}
