package com.kaaval.app

import android.app.Application
import android.util.Log

class KaavalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("KaavalApplication", "KAAVAL Accessibility Emergency Ecosystem Application Initialized")
    }
}
