package com.kaaval.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class KaavalLocationManager(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            
            // Try to get fresh location with timeout
            val freshLocation = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
            
            if (freshLocation != null) return freshLocation

            // Fallback to last known location if fresh acquisition fails (Differentiator #1)
            Log.w("LocationManager", "Fresh GPS lock failed. Falling back to last known location.")
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            Log.e("LocationManager", "GPS acquisition error", e)
            null
        }
    }
}
