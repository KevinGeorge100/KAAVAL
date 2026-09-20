package com.kaaval.app.domain.model

/**
 * Domain model for a secure live tracking session with caregiver coordination & AI telemetry.
 */
data class TrackingSession(
    val incidentId: String,
    val status: TrackingStatus,
    val createdAt: Long,
    val expiresAt: Long,
    val latestLocation: LocationData? = null,
    val claimedBy: String? = null,
    val claimedEta: String? = null,
    val claimedNote: String? = null,
    val aiSummary: String? = null,
    val audioTranscript: String? = null,
    val lastReassurancePing: Long? = null
)

enum class TrackingStatus {
    ACTIVE,
    RESPONDING,
    COMPLETED,
    EXPIRED
}
