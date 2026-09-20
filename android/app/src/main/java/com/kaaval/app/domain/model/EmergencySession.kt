package com.kaaval.app.domain.model

/**
 * KAAVAL Emergency Session
 * Represents the persistent identity and lifecycle of an active emergency incident.
 */
data class EmergencySession(
    val incidentId: String,
    val startedAt: Long,
    val status: SessionStatus,
    val lastKnownLocation: LocationData? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
)

enum class SessionStatus {
    IDLE,
    ACTIVE,
    COMPLETING,
    COMPLETED,
    FAILED
}
