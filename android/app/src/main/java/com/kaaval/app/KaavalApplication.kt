package com.kaaval.app

import android.app.Application
import android.util.Log
import com.kaaval.app.accessibility.HapticFeedbackManager
import com.kaaval.app.accessibility.VoiceFeedbackManager

/**
 * KAAVAL Application Class
 * Initializes core accessibility engines (VoiceFeedbackManager and HapticFeedbackManager Singletons) on application startup.
 */
class KaavalApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("KaavalApplication", "KAAVAL Accessibility Emergency Ecosystem Application Initializing...")
        
        // Auto-initialize accessibility engines on startup
        VoiceFeedbackManager.initialize(this)
        HapticFeedbackManager.initialize(this)
    }
}
