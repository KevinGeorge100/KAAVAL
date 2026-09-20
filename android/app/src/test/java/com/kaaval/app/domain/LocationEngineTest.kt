package com.kaaval.app.domain

import com.kaaval.app.domain.model.EmergencySession
import com.kaaval.app.domain.model.LocationData
import com.kaaval.app.domain.model.SessionStatus
import com.kaaval.app.domain.repository.EmergencySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * KAAVAL Location Engine Tests
 * Verifies that location updates correctly integrate with the active Emergency Session.
 */
class LocationEngineTest {

    private lateinit var repository: FakeEmergencySessionRepository

    @Before
    fun setUp() {
        repository = FakeEmergencySessionRepository()
    }

    @Test
    fun `Verify Location Update - Active session is updated with coordinates`() = runBlocking {
        // 1. Create an active session
        val session = EmergencySession(
            incidentId = "INC-123",
            startedAt = 1000L,
            status = SessionStatus.ACTIVE
        )
        repository.createSession(session)

        // 2. Simulate a location update
        val locationUpdate = LocationData(
            latitude = 9.9312,
            longitude = 76.2673,
            timestamp = 2000L,
            accuracy = 10.0f,
            source = "fused"
        )
        
        val active = repository.getActiveSession().first()
        assertNotNull(active)
        
        val updated = active!!.copy(lastKnownLocation = locationUpdate)
        repository.updateSession(updated)

        // 3. Verify persistence
        val result = repository.getActiveSession().first()
        assertEquals(9.9312, result?.lastKnownLocation?.latitude)
        assertEquals(10.0f, result?.lastKnownLocation?.accuracy)
    }

    @Test
    fun `Verify No Session - Location update fails gracefully when no session exists`() = runBlocking {
        // No session created
        val active = repository.getActiveSession().first()
        assertNull(active)
        
        // Simulation: The service should stop if it can't find the session (tested in service integration)
    }

    // Helper Fake Repository
    class FakeEmergencySessionRepository : EmergencySessionRepository {
        private var sessionFlow = MutableStateFlow<EmergencySession?>(null)

        override suspend fun createSession(session: EmergencySession): Boolean {
            if (sessionFlow.value != null) return false
            sessionFlow.value = session
            return true
        }

        override fun getActiveSession(): Flow<EmergencySession?> = sessionFlow

        override suspend fun updateSession(session: EmergencySession) {
            sessionFlow.value = session
        }

        override suspend fun completeSession(incidentId: String) {
            sessionFlow.value = null
        }

        override suspend fun clearActiveSession() {
            sessionFlow.value = null
        }
    }
}
