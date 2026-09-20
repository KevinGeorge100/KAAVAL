package com.kaaval.app.domain.model

/**
 * Domain model for a secure live tracking session.
 */
data class TrackingSession(
    val incidentId: String,
    val status: TrackingStatus,
    val createdAt: Long,
    val expiresAt: Long,
    val latestLocation: LocationData? = null
)

enum class TrackingStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED
}
