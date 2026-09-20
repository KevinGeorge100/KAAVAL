package com.kaaval.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kaaval.app.domain.model.EmergencySession
import com.kaaval.app.domain.model.SessionStatus

@Entity(tableName = "emergency_sessions")
data class EmergencySessionEntity(
    @PrimaryKey val incidentId: String,
    val startedAt: Long,
    val status: String,
    val lastKnownLatitude: Double?,
    val lastKnownLongitude: Double?,
    val lastKnownAccuracy: Float?,
    val lastLocationTimestamp: Long?,
    val createdAt: Long,
    val endedAt: Long?
) {
    fun toDomainModel(): EmergencySession {
        return EmergencySession(
            incidentId = incidentId,
            startedAt = startedAt,
            status = SessionStatus.valueOf(status),
            lastKnownLocation = if (lastKnownLatitude != null && lastKnownLongitude != null) {
                com.kaaval.app.domain.model.LocationData(
                    latitude = lastKnownLatitude,
                    longitude = lastKnownLongitude,
                    accuracy = lastKnownAccuracy ?: 0f,
                    timestamp = lastLocationTimestamp ?: 0L
                )
            } else null,
            createdAt = createdAt,
            endedAt = endedAt
        )
    }

    companion object {
        fun fromDomainModel(session: EmergencySession): EmergencySessionEntity {
            return EmergencySessionEntity(
                incidentId = session.incidentId,
                startedAt = session.startedAt,
                status = session.status.name,
                lastKnownLatitude = session.lastKnownLocation?.latitude,
                lastKnownLongitude = session.lastKnownLocation?.longitude,
                lastKnownAccuracy = session.lastKnownLocation?.accuracy,
                lastLocationTimestamp = session.lastKnownLocation?.timestamp,
                createdAt = session.createdAt,
                endedAt = session.endedAt
            )
        }
    }
}
