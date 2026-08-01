package com.kaaval.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kaaval.app.domain.model.EmergencyState

@Entity(tableName = "emergency_state")
data class EmergencyStateEntity(
    @PrimaryKey val id: Int = 1,
    val incidentId: String,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val trackingUrl: String,
    val isPrimaryCalled: Boolean,
    val respondingCaregiver: String?
) {
    fun toDomainModel(): EmergencyState.Active {
        return EmergencyState.Active(
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
        fun fromDomainModel(state: EmergencyState.Active): EmergencyStateEntity {
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
