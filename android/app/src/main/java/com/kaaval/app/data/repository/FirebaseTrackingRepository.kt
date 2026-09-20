package com.kaaval.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kaaval.app.domain.model.LocationData
import com.kaaval.app.domain.model.TrackingSession
import com.kaaval.app.domain.model.TrackingStatus
import com.kaaval.app.domain.repository.LocationTrackingRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

class FirebaseTrackingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LocationTrackingRepository {

    private val incidentsCollection = firestore.collection("incidents")

    override suspend fun createTrackingSession(incidentId: String): Boolean {
        return try {
            val now = System.currentTimeMillis()
            val expiresAt = now + TimeUnit.HOURS.toMillis(4) // 4-hour expiration

            val data = hashMapOf(
                "incidentId" to incidentId,
                "status" to "ACTIVE",
                "createdAt" to Date(now),
                "expiresAt" to Date(expiresAt),
                "userName" to "Visually Impaired User" // Placeholder
            )

            incidentsCollection.document(incidentId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseTracking", "Failed to create session: ${e.message}")
            false
        }
    }

    override suspend fun publishLocation(incidentId: String, location: LocationData): Boolean {
        return try {
            val data = hashMapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "accuracy" to location.accuracy,
                "lastUpdate" to Date(location.timestamp),
                "status" to "ACTIVE"
            )

            incidentsCollection.document(incidentId)
                .update(data as Map<String, Any>)
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseTracking", "Failed to publish location: ${e.message}")
            false
        }
    }

    override suspend fun completeTrackingSession(incidentId: String): Boolean {
        return try {
            incidentsCollection.document(incidentId)
                .update("status", "COMPLETED", "endedAt", Date())
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseTracking", "Failed to complete session: ${e.message}")
            false
        }
    }

    override fun getTrackingSession(incidentId: String): Flow<TrackingSession?> = callbackFlow {
        val subscription = incidentsCollection.document(incidentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val statusStr = snapshot.getString("status") ?: "ACTIVE"
                    val lat = snapshot.getDouble("latitude")
                    val lng = snapshot.getDouble("longitude")
                    val acc = snapshot.getDouble("accuracy")?.toFloat()
                    val time = snapshot.getDate("lastUpdate")?.time
                    val created = snapshot.getDate("createdAt")?.time ?: 0L
                    val expires = snapshot.getDate("expiresAt")?.time ?: 0L

                    val location = if (lat != null && lng != null) {
                        LocationData(lat, lng, time ?: 0L, acc ?: 0f, snapshot.getString("source"))
                    } else null

                    trySend(TrackingSession(
                        incidentId = incidentId,
                        status = TrackingStatus.valueOf(statusStr),
                        createdAt = created,
                        expiresAt = expires,
                        latestLocation = location
                    ))
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }
}
