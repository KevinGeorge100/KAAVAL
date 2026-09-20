package com.kaaval.app.domain.model

/**
 * Domain model for GPS coordinates and metadata.
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val accuracy: Float,
    val source: String? = null
)
