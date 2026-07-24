package com.kaaval.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kaaval.app.domain.model.EmergencyIncident

@Entity(tableName = "incident_logs")
data class IncidentEntity(
    @PrimaryKey val incidentId: String,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val status: String,
    val trackingUrl: String
) {
    fun toDomainModel(): EmergencyIncident {
        return EmergencyIncident(
            incidentId = incidentId,
            timestamp = timestamp,
            latitude = latitude,
            longitude = longitude,
            status = status,
            trackingUrl = trackingUrl
        )
    }

    companion object {
        fun fromDomainModel(incident: EmergencyIncident): IncidentEntity {
            return IncidentEntity(
                incidentId = incident.incidentId,
                timestamp = incident.timestamp,
                latitude = incident.latitude,
                longitude = incident.longitude,
                status = incident.status,
                trackingUrl = incident.trackingUrl
            )
        }
    }
}
