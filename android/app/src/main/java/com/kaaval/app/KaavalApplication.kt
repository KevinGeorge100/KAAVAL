package com.kaaval.app

import android.app.Application
import android.util.Log
import com.kaaval.app.accessibility.VoiceFeedbackManager

/**
 * KAAVAL Application Class
 * Initializes core accessibility engines (VoiceFeedbackManager Singleton) on application startup.
 */
class KaavalApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("KaavalApplication", "KAAVAL Accessibility Emergency Ecosystem Application Initializing...")
        
        // Auto-initialize VoiceFeedbackManager Singleton on startup
        VoiceFeedbackManager.initialize(this)
    }
}
