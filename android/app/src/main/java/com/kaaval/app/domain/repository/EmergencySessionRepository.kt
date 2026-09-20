package com.kaaval.app.domain.repository

import com.kaaval.app.domain.model.EmergencySession
import kotlinx.coroutines.flow.Flow

/**
 * Emergency Session Repository Abstraction
 * Ensures the domain layer is independent of the platform's persistence implementation.
 */
interface EmergencySessionRepository {
    
    /**
     * Creates a new persistent emergency session.
     * Should fail if an active session already exists.
     */
    suspend fun createSession(session: EmergencySession): Boolean

    /**
     * Returns a Flow of the currently active session, or null if none exists.
     */
    fun getActiveSession(): Flow<EmergencySession?>

    /**
     * Updates an existing session (e.g. status, location).
     */
    suspend fun updateSession(session: EmergencySession)

    /**
     * Marks the session as completed and records the end time.
     */
    suspend fun completeSession(incidentId: String)

    /**
     * Permanently clears any active session record.
     */
    suspend fun clearActiveSession()
}
