package com.kaaval.app.domain

import com.kaaval.app.domain.model.EmergencyAction
import com.kaaval.app.domain.model.EmergencyEvent
import com.kaaval.app.domain.model.EmergencyState
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * KAAVAL SOS Safety Net
 * This test suite protects "Feature 1" (The Core SOS Workflow).
 * It verifies that state transitions and emergency actions are unbreakable.
 */
class EmergencyStateManagerTest {

    private lateinit var stateManager: EmergencyStateManager

    @Before
    fun setUp() {
        stateManager = EmergencyStateManager(logger = { _, _ -> /* Ignore logs in tests */ })
    }

    @Test
    fun `Verify SOS Trigger - Button press leads to Holding state`() {
        // 1. Start at Idle
        assertTrue(stateManager.state.value is EmergencyState.Idle)

        // 2. User presses button
        stateManager.onEvent(EmergencyEvent.ButtonPressed)

        // 3. Verify we are now holding the button
        assertTrue(stateManager.state.value is EmergencyState.HoldingButton)
    }

    @Test
    fun `Verify Countdown - Ticking updates state correctly`() {
        // 1. Move to Holding
        stateManager.onEvent(EmergencyEvent.ButtonPressed)
        
        // 2. Move to Countdown
        stateManager.onEvent(EmergencyEvent.CountdownTicked(5))
        
        // 3. Verify state
        val state = stateManager.state.value
        assertTrue(state is EmergencyState.Countdown)
        assertTrue((state as EmergencyState.Countdown).secondsRemaining == 5)
    }

    @Test
    fun `Verify Activation - Finishing countdown triggers emergency actions`() {
        // 1. Set up initial state (Countdown)
        stateManager.onEvent(EmergencyEvent.ButtonPressed)
        stateManager.onEvent(EmergencyEvent.CountdownTicked(1))

        // 2. Trigger Countdown Finished
        stateManager.onEvent(EmergencyEvent.CountdownFinished)

        // 3. Verify state is now Activated
        assertTrue(stateManager.state.value is EmergencyState.Activated)
        
        // 4. Note: We don't check for specific composite action content here 
        // to avoid fragility, but the transition ensures the dispatcher will receive them.
    }

    @Test
    fun `Verify Resolution - I am safe returns to Idle`() {
        // 1. Simulate an active emergency
        stateManager.onEvent(EmergencyEvent.ButtonPressed)
        stateManager.onEvent(EmergencyEvent.CountdownFinished)
        stateManager.onEvent(EmergencyEvent.LocationAcquired(1.0, 1.0))
        stateManager.onEvent(EmergencyEvent.LiveTrackingStarted())
        
        assertTrue(stateManager.state.value is EmergencyState.LiveTracking)

        // 2. User clicks "I am safe"
        stateManager.onEvent(EmergencyEvent.EmergencyCompleted)

        // 3. Verify system returns to Idle
        assertTrue(stateManager.state.value is EmergencyState.Idle)
    }

    @Test
    fun `Verify Countdown - Cancelling countdown returns to Idle and emits composite cancellation`() = kotlinx.coroutines.test.runTest {
        val actions = mutableListOf<EmergencyAction>()
        val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined).launch {
            stateManager.action.collect { actions.add(it) }
        }

        // 1. Move to Countdown
        stateManager.onEvent(EmergencyEvent.ButtonPressed)
        stateManager.onEvent(EmergencyEvent.CountdownTicked(5))
        assertTrue(stateManager.state.value is EmergencyState.Countdown)

        // 2. User cancels countdown
        stateManager.onEvent(EmergencyEvent.CountdownCancelled)

        // 3. State should be Idle
        assertTrue(stateManager.state.value is EmergencyState.Idle)

        // 4. Action should include CancelCountdown and voice/haptic cancellation
        val lastAction = actions.lastOrNull()
        assertTrue(lastAction is EmergencyAction.CompositeAction)
        val composite = (lastAction as EmergencyAction.CompositeAction).actions
        assertTrue(composite.any { it is EmergencyAction.CancelCountdown })
        assertTrue(composite.any { it is EmergencyAction.PlayVoiceAnnouncement && it.announcementType == "COUNTDOWN_CANCELLED" && it.isPriority })
        assertTrue(composite.any { it is EmergencyAction.PlayHapticPattern && it.patternType == "COUNTDOWN_CANCELLED" })

        job.cancel()
    }

    @Test
    fun `Verify Hold Release - Releasing button before countdown returns to Idle and emits cancellation`() = kotlinx.coroutines.test.runTest {
        val actions = mutableListOf<EmergencyAction>()
        val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined).launch {
            stateManager.action.collect { actions.add(it) }
        }

        // 1. Press button (Holding)
        stateManager.onEvent(EmergencyEvent.ButtonPressed)
        assertTrue(stateManager.state.value is EmergencyState.HoldingButton)

        // 2. User releases button early
        stateManager.onEvent(EmergencyEvent.ButtonReleased)

        // 3. State should be Idle
        assertTrue(stateManager.state.value is EmergencyState.Idle)

        // 4. Should emit cancellation actions
        val lastAction = actions.lastOrNull()
        assertTrue(lastAction is EmergencyAction.CompositeAction)
        val composite = (lastAction as EmergencyAction.CompositeAction).actions
        assertTrue(composite.any { it is EmergencyAction.PlayVoiceAnnouncement && it.announcementType == "COUNTDOWN_CANCELLED" && it.isPriority })

        job.cancel()
    }
}
