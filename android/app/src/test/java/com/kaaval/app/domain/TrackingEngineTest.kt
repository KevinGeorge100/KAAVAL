package com.kaaval.app.domain

import com.kaaval.app.domain.model.LocationData
import com.kaaval.app.domain.model.TrackingSession
import com.kaaval.app.domain.model.TrackingStatus
import com.kaaval.app.domain.repository.LocationTrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * KAAVAL Secure Live Tracking Engine Tests
 * Verifies that the tracking repository correctly manages sessions and location publishing.
 */
class TrackingEngineTest {

    private lateinit var repository: FakeLocationTrackingRepository

    @Before
    fun setUp() {
        repository = FakeLocationTrackingRepository()
    }

    @Test
    fun `Verify Create Session - Tracking session is created on backend`() = runBlocking {
        val incidentId = "KVL-123"
        val success = repository.createTrackingSession(incidentId)
        assertTrue(success)

        val session = repository.getTrackingSession(incidentId).first()
        assertNotNull(session)
        assertEquals(incidentId, session?.incidentId)
        assertEquals(TrackingStatus.ACTIVE, session?.status)
    }

    @Test
    fun `Verify Publish Location - Location is updated in active session`() = runBlocking {
        val incidentId = "KVL-123"
        repository.createTrackingSession(incidentId)

        val location = LocationData(9.9312, 76.2673, 1000L, 5f)
        val success = repository.publishLocation(incidentId, location)
        assertTrue(success)

        val session = repository.getTrackingSession(incidentId).first()
        assertEquals(9.9312, session?.latestLocation?.latitude)
        assertEquals(5f, session?.latestLocation?.accuracy)
    }

    @Test
    fun `Verify Session Completion - Status moves to COMPLETED`() = runBlocking {
        val incidentId = "KVL-123"
        repository.createTrackingSession(incidentId)
        
        repository.completeTrackingSession(incidentId)

        val session = repository.getTrackingSession(incidentId).first()
        assertEquals(TrackingStatus.COMPLETED, session?.status)
    }

    // Helper Fake Repository
    class FakeLocationTrackingRepository : LocationTrackingRepository {
        private val sessions = mutableMapOf<String, MutableStateFlow<TrackingSession?>>()

        override suspend fun createTrackingSession(incidentId: String): Boolean {
            val session = TrackingSession(
                incidentId = incidentId,
                status = TrackingStatus.ACTIVE,
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 3600000
            )
            sessions[incidentId] = MutableStateFlow(session)
            return true
        }

        override suspend fun publishLocation(incidentId: String, location: LocationData): Boolean {
            val flow = sessions[incidentId] ?: return false
            val current = flow.value ?: return false
            flow.value = current.copy(latestLocation = location)
            return true
        }

        override suspend fun completeTrackingSession(incidentId: String): Boolean {
            val flow = sessions[incidentId] ?: return false
            val current = flow.value ?: return false
            flow.value = current.copy(status = TrackingStatus.COMPLETED)
            return true
        }

        override fun getTrackingSession(incidentId: String): Flow<TrackingSession?> {
            return sessions[incidentId] ?: MutableStateFlow(null)
        }
    }
}
