package com.kaaval.app.domain.action.handlers

import android.util.Log
import com.kaaval.app.domain.model.EmergencyAction
import com.kaaval.app.domain.model.EmergencyEvent
import com.kaaval.app.domain.model.LocationData
import com.kaaval.app.domain.repository.EmergencySessionRepository
import com.kaaval.app.service.KaavalLocationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * KAAVAL Location Action Handler
 * Responsible for acquiring GPS coordinates during an emergency.
 * Updates the active session with the initial fix.
 */
class LocationActionHandler(
    private val locationManager: KaavalLocationManager,
    private val repository: EmergencySessionRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    fun handle(action: EmergencyAction, onActionHandled: (EmergencyEvent) -> Unit) {
        if (action !is EmergencyAction.AcquireLocation) return

        Log.i("LocationHandler", "Acquiring high-accuracy location...")
        
        if (!locationManager.isLocationEnabled()) {
            Log.e("LocationHandler", "LOCATION_PROVIDER_DISABLED")
            onActionHandled(EmergencyEvent.ErrorOccurred("Please enable GPS for safety tracking."))
            return
        }

        scope.launch {
            val location = locationManager.getCurrentLocation()
            if (location != null) {
                val locationData = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = location.time,
                    accuracy = location.accuracy,
                    source = location.provider
                )
                
                // Persist the fix to the active session immediately
                val activeSession = repository.getActiveSession().first()
                activeSession?.let {
                    repository.updateSession(it.copy(lastKnownLocation = locationData))
                    Log.i("LocationHandler", "LOCATION_UPDATE_PERSISTED accuracy=${location.accuracy}m")
                }

                onActionHandled(EmergencyEvent.LocationAcquired(location.latitude, location.longitude))
            } else {
                Log.e("LocationHandler", "LOCATION_TIMEOUT")
                onActionHandled(EmergencyEvent.ErrorOccurred("GPS signal lost. Check location settings."))
            }
        }
    }
}
