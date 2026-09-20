package com.kaaval.app.domain

import com.kaaval.app.domain.model.EmergencyEvent
import com.kaaval.app.domain.model.EmergencyState
import com.kaaval.app.domain.model.EmergencySession
import com.kaaval.app.domain.model.LocationData
import com.kaaval.app.domain.model.SessionStatus
import com.kaaval.app.domain.repository.EmergencySessionRepository
import com.kaaval.app.ui.viewmodel.EmergencyViewModel
import com.kaaval.app.domain.action.EmergencyActionDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class Bug3B01RegressionTest {

    private lateinit var repository: FakeEmergencySessionRepository
    private lateinit var stateManager: EmergencyStateManager
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeEmergencySessionRepository()
        stateManager = EmergencyStateManager(logger = { _, _ -> })
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `TEST 1 & 4 - Verify recovery from existing session moves to LiveTracking once`() = runTest {
        // 1. Setup a persistent active session in the repository
        val incidentId = "KVL-123"
        repository.createSession(EmergencySession(incidentId, 1000L, SessionStatus.ACTIVE))

        // 2. Initialize ViewModel (Simulating Activity start/recovery)
        val viewModel = EmergencyViewModel(mock(EmergencyActionDispatcher::class.java), repository)
        
        // 3. Verify state is LiveTracking (Recovered)
        val state = viewModel.emergencyState.value
        assertTrue("State should be LiveTracking after recovery", state is EmergencyState.LiveTracking)
        assertEquals(incidentId, (state as EmergencyState.LiveTracking).incidentId)

        // 4. Update session data (Simulating location update in background)
        repository.updateSession(repository.getActiveSession().first()!!.copy(
            lastKnownLocation = LocationData(1.0, 1.0, 2000L, 5f)
        ))
        
        // 5. Verify state is STILL LiveTracking and no loop occurred
        assertTrue("State should remain LiveTracking", viewModel.emergencyState.value is EmergencyState.LiveTracking)
    }

    @Test
    fun `TEST 2 & 3 - Verify location updates do not cause state machine transitions`() = runTest {
        // 1. Start emergency normally using valid transitions
        stateManager.onEvent(EmergencyEvent.ButtonPressed)
        stateManager.onEvent(EmergencyEvent.CountdownFinished)
        stateManager.onEvent(EmergencyEvent.LocationAcquired(1.0, 1.0))
        stateManager.onEvent(EmergencyEvent.LiveTrackingStarted(1.0, 1.0))

        val initialState = stateManager.state.value
        assertTrue("Initial state should be LiveTracking", initialState is EmergencyState.LiveTracking)

        // 2. Simulate data updates (Repository/Room)
        // These should NOT affect the StateManager directly
        repository.updateSession(EmergencySession("INC", 100L, SessionStatus.ACTIVE))
        
        // 3. Verify state remains same
        assertEquals("State should not change on data update", initialState, stateManager.state.value)
    }

    @Test
    fun `TEST 7 - Completing the session still transitions correctly`() = runTest {
        // Use SessionRecovered to get into LiveTracking quickly
        stateManager.onEvent(EmergencyEvent.SessionRecovered("INC", 1.0, 1.0))
        
        assertTrue("Should be in LiveTracking", stateManager.state.value is EmergencyState.LiveTracking)
        
        stateManager.onEvent(EmergencyEvent.EmergencyCompleted)
        assertTrue("Should be back in Idle", stateManager.state.value is EmergencyState.Idle)
    }

    @Test
    fun `TEST 8 - Duplicate SOS remains rejected`() = runTest {
        stateManager.onEvent(EmergencyEvent.ButtonPressed)
        stateManager.onEvent(EmergencyEvent.CountdownFinished)
        
        val activeState = stateManager.state.value
        assertTrue(activeState is EmergencyState.Activated)
        
        // Try another button press
        stateManager.onEvent(EmergencyEvent.ButtonPressed)
        
        // Should still be in Activated, not HoldingButton
        assertEquals("Re-trigger should be ignored in Activated state", activeState, stateManager.state.value)
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
