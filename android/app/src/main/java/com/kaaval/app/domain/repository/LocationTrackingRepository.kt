package com.kaaval.app.domain.repository

import com.kaaval.app.domain.model.LocationData
import com.kaaval.app.domain.model.TrackingSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing live location tracking with a secure backend.
 */
interface LocationTrackingRepository {

    /**
     * Creates a new tracking session on the backend.
     */
    suspend fun createTrackingSession(incidentId: String): Boolean

    /**
     * Publishes a new location update to the active session.
     */
    suspend fun publishLocation(incidentId: String, location: LocationData): Boolean

    /**
     * Marks the tracking session as completed.
     */
    suspend fun completeTrackingSession(incidentId: String): Boolean

    /**
     * Returns a Flow of the tracking session state from the backend.
     */
    fun getTrackingSession(incidentId: String): Flow<TrackingSession?>
}
