package com.kaaval.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kaaval.app.domain.model.EmergencyState

/**
 * Persists the current state of an emergency.
 * This is the "Memory" of the app that survives crashes or reboots.
 */
@Entity(tableName = "emergency_current_state")
data class EmergencyStateEntity(
    @PrimaryKey val id: Int = 0, // Singleton record
    val incidentId: String,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val trackingUrl: String,
    val isPrimaryCalled: Boolean,
    val respondingCaregiver: String?
) {
    fun toDomainModel(): EmergencyState.LiveTracking {
        return EmergencyState.LiveTracking(
            incidentId = incidentId,
            timestamp = timestamp,
            latitude = latitude,
            longitude = longitude,
            trackingUrl = trackingUrl,
            isPrimaryCalled = isPrimaryCalled,
            respondingCaregiver = respondingCaregiver
        )
    }

    companion object {
        fun fromDomainModel(state: EmergencyState.LiveTracking): EmergencyStateEntity {
            return EmergencyStateEntity(
                incidentId = state.incidentId,
                timestamp = state.timestamp,
                latitude = state.latitude,
                longitude = state.longitude,
                trackingUrl = state.trackingUrl,
                isPrimaryCalled = state.isPrimaryCalled,
                respondingCaregiver = state.respondingCaregiver
            )
        }
    }
}
