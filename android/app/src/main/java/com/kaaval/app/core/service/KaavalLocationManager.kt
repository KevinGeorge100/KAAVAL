package com.kaaval.app.core.service

import android.content.Context
import android.location.Location

/**
 * Location Manager component under com.kaaval.app.core.service.
 */
class KaavalLocationManager(private val context: Context) {

    suspend fun getCurrentLocation(): Location? {
        return null
    }
}
