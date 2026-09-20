package com.kaaval.app.domain.action.handlers

import android.util.Log
import com.kaaval.app.accessibility.VoiceFeedbackManager
import com.kaaval.app.domain.model.EmergencyAction

/**
 * KAAVAL Voice Action Handler
 * Responsible for managing TTS announcements and safely bridging with VoiceFeedbackManager.
 */
class VoiceActionHandler {
    fun handle(action: EmergencyAction) {
        if (action !is EmergencyAction.PlayVoiceAnnouncement) return

        Log.d("VoiceActionHandler", "Handling: ${action.announcementType}")
        
        // Specialized handling for numeric countdown ticks
        val seconds = action.announcementType.toIntOrNull()
        if (seconds != null) {
            VoiceFeedbackManager.speakCountdown(seconds)
            return
        }

        try {
            val announcementType = VoiceFeedbackManager.AnnouncementType.valueOf(action.announcementType)
            VoiceFeedbackManager.announce(announcementType, action.isPriority)
        } catch (e: Exception) {
            // Fallback for custom or legacy strings
            if (action.isPriority) {
                VoiceFeedbackManager.speakPriority(action.announcementType)
            } else {
                VoiceFeedbackManager.speak(action.announcementType)
            }
        }
    }
}
