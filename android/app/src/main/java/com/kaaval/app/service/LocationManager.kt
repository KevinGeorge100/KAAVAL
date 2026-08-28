package com.kaaval.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager as AndroidLocationManager
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.kaaval.app.domain.model.LocationData
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Policy for location updates frequency.
 */
data class LocationUpdatePolicy(
    val interval: Duration = 15.seconds,
    val fastestInterval: Duration = 5.seconds
)

/**
 * KAAVAL Location Manager
 * Reliable multi-stage location provider using FusedLocationProviderClient.
 * Supports high-accuracy continuous updates during emergency sessions.
 */
class KaavalLocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback? = null

    fun isLocationEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
        return lm.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
               lm.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
    }

    /**
     * Attempts to acquire high-accuracy coordinates with fallbacks.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        Log.d("LocationManager", "Attempting to get high-accuracy location with 10s timeout...")
        return try {
            val freshLocation = withTimeoutOrNull(10.seconds) {
                val cancellationTokenSource = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()
            }
            
            if (freshLocation != null) {
                Log.i("LocationManager", "LOCATION_UPDATE_RECEIVED source=fused_fresh")
                return freshLocation
            }

            // Fallback 1: Last Known Location
            Log.w("LocationManager", "LOCATION_TIMEOUT. Checking last known location...")
            val lastKnown = fusedLocationClient.lastLocation.await()
            if (lastKnown != null) {
                Log.i("LocationManager", "LOCATION_LAST_KNOWN_RECEIVED source=fused_last")
                return lastKnown
            }

            // Fallback 2: Direct Location Manager (Final Boss Fallback)
            Log.w("LocationManager", "Fused Location failed. Trying direct GPS provider...")
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
            val gpsLocation = lm.getLastKnownLocation(AndroidLocationManager.GPS_PROVIDER)
            val netLocation = lm.getLastKnownLocation(AndroidLocationManager.NETWORK_PROVIDER)
            
            val finalFallback = gpsLocation ?: netLocation
            if (finalFallback != null) {
                Log.i("LocationManager", "LOCATION_LAST_KNOWN_RECEIVED source=android_direct")
                return finalFallback
            }

            Log.e("LocationManager", "LOCATION_PROVIDER_DISABLED or all fallbacks exhausted.")
            null
        } catch (e: Exception) {
            Log.e("LocationManager", "GPS acquisition critical error: ${e.message}", e)
            null
        }
    }

    /**
     * Starts continuous location updates owned by the Foreground Service.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(
        policy: LocationUpdatePolicy = LocationUpdatePolicy(),
        onUpdate: (LocationData) -> Unit
    ) {
        if (locationCallback != null) {
            Log.w("LocationManager", "Duplicate location update request ignored.")
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, policy.interval.inWholeMilliseconds)
            .setMinUpdateIntervalMillis(policy.fastestInterval.inWholeMilliseconds)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    onUpdate(LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = location.time,
                        accuracy = location.accuracy,
                        source = location.provider
                    ))
                    Log.d("LocationManager", "LOCATION_UPDATE_RECEIVED accuracy=${location.accuracy}m")
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback!!,
            Looper.getMainLooper()
        )
        Log.i("LocationManager", "LOCATION_SERVICE_STARTED interval=${policy.interval}")
    }

    /**
     * Stops the continuous location stream.
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
            Log.i("LocationManager", "LOCATION_SERVICE_STOPPED")
        }
    }
}
