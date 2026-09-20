package com.kaaval.app.domain

import com.kaaval.app.domain.model.EmergencySession
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
 * KAAVAL Emergency Session Tests
 * Verifies the persistent session foundation and the single active session rule.
 */
class EmergencySessionTest {

    private lateinit var repository: FakeEmergencySessionRepository

    @Before
    fun setUp() {
        repository = FakeEmergencySessionRepository()
    }

    @Test
    fun `Verify Create Session - New session is stored correctly`() = runBlocking {
        val session = EmergencySession(
            incidentId = "TEST-1",
            startedAt = 1000L,
            status = SessionStatus.ACTIVE
        )

        val success = repository.createSession(session)
        assertTrue(success)

        val active = repository.getActiveSession().first()
        assertNotNull(active)
        assertEquals("TEST-1", active?.incidentId)
        assertEquals(SessionStatus.ACTIVE, active?.status)
    }

    @Test
    fun `Verify Single Active Session - Reject duplicate activation`() = runBlocking {
        val session1 = EmergencySession("TEST-1", 1000L, SessionStatus.ACTIVE)
        val session2 = EmergencySession("TEST-2", 2000L, SessionStatus.ACTIVE)

        assertTrue(repository.createSession(session1))
        
        // Second attempt should fail
        assertFalse(repository.createSession(session2))

        val active = repository.getActiveSession().first()
        assertEquals("TEST-1", active?.incidentId)
    }

    @Test
    fun `Verify Session Completion - Completing session clears active status`() = runBlocking {
        val session = EmergencySession("TEST-1", 1000L, SessionStatus.ACTIVE)
        repository.createSession(session)
        
        repository.completeSession("TEST-1")

        val active = repository.getActiveSession().first()
        assertNull(active)
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
