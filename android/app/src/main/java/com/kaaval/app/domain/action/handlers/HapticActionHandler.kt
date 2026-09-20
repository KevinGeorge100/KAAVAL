package com.kaaval.app.domain.action.handlers

import android.util.Log
import com.kaaval.app.accessibility.HapticFeedbackManager
import com.kaaval.app.domain.model.EmergencyAction

/**
 * KAAVAL Haptic Action Handler
 * Responsible for managing tactile patterns and safely bridging with HapticFeedbackManager.
 */
class HapticActionHandler {
    fun handle(action: EmergencyAction) {
        if (action !is EmergencyAction.PlayHapticPattern) return

        Log.d("HapticActionHandler", "Handling: ${action.patternType}")

        try {
            val pattern = HapticFeedbackManager.HapticPattern.valueOf(action.patternType)
            HapticFeedbackManager.vibrate(pattern)
        } catch (e: Exception) {
            // Fallback for custom strings
            when (action.patternType) {
                "HOLD_TICK" -> HapticFeedbackManager.vibrate(HapticFeedbackManager.HapticPattern.COUNTDOWN_TICK)
                "SUCCESS" -> HapticFeedbackManager.vibrate(HapticFeedbackManager.HapticPattern.SUCCESS)
                else -> Log.w("HapticActionHandler", "Unknown pattern: ${action.patternType}")
            }
        }
    }
}
