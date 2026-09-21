package com.kaaval.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kaaval.app.domain.EmergencyStateManager
import com.kaaval.app.domain.action.EmergencyActionDispatcher
import com.kaaval.app.domain.model.EmergencyAction
import com.kaaval.app.domain.model.EmergencyEvent
import com.kaaval.app.domain.model.EmergencyState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * KAAVAL Emergency ViewModel
 * Exposes the emergency state and actions to the UI.
 * Bridges the State Machine with the Action Dispatcher.
 */
class EmergencyViewModel(
    private val actionDispatcher: EmergencyActionDispatcher,
    private val repository: com.kaaval.app.domain.repository.EmergencySessionRepository
) : ViewModel() {

    private val stateManager = EmergencyStateManager()
    
    val emergencyState: StateFlow<EmergencyState> = stateManager.state

    private var holdingJob: Job? = null
    private var countdownJob: Job? = null
    private var simulationJob: Job? = null
    var isConfirmingSafe by mutableStateOf(value = false)
    var isDiscreetMode = false
        set(value) {
            field = value
            actionDispatcher.isDiscreetMode = value
        }

    init {
        // Recovery Logic: Restore active session state on startup
        viewModelScope.launch {
            repository.getActiveSession().collect { session ->
                val currentState = emergencyState.value
                if (session != null && (currentState is EmergencyState.Idle)) {
                    android.util.Log.i("EmergencyViewModel", "SESSION_RECOVERED incidentId=${session.incidentId}")
                    processEvent(EmergencyEvent.SessionRecovered(
                        incidentId = session.incidentId,
                        latitude = session.lastKnownLocation?.latitude, 
                        longitude = session.lastKnownLocation?.longitude
                    ))
                }
            }
        }

        // Bridge between StateManager and ActionDispatcher
        viewModelScope.launch {
            stateManager.action.collect { action ->
                actionDispatcher.dispatch(action) { event ->
                    processEvent(event)
                }
                handleSimulatedActionResponse(action)
            }
        }

        // Connect cloud caregiver acknowledgment to UI State
        viewModelScope.launch {
            emergencyState.collect { state ->
                if (state is EmergencyState.LiveTracking) {
                    observeTrackingSession(state.incidentId)
                } else if (state is EmergencyState.Idle || state is EmergencyState.Completed || state is EmergencyState.Cancelled || state is EmergencyState.Resolved) {
                    trackingObservationJob?.cancel()
                    trackingObservationJob = null
                }
            }
        }
    }

    private var trackingObservationJob: Job? = null

    private fun observeTrackingSession(incidentId: String) {
        if (trackingObservationJob?.isActive == true) return
        trackingObservationJob = viewModelScope.launch {
            try {
                com.kaaval.app.data.repository.FirebaseTrackingRepository()
                    .getTrackingSession(incidentId)
                    .collect { session ->
                        if (session != null && !session.claimedBy.isNullOrBlank()) {
                            processEvent(EmergencyEvent.CaregiverAcknowledged(session.claimedBy, session.claimedEta))
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.w("EmergencyViewModel", "Error observing tracking session: ${e.message}")
            }
        }
    }

    fun onSosButtonPressed() {
        if (emergencyState.value !is EmergencyState.Idle) return
        
        processEvent(EmergencyEvent.ButtonPressed)
        
        holdingJob?.cancel()
        holdingJob = viewModelScope.launch {
            val holdingDuration = 3000L
            val intervals = 30
            for (i in 1..intervals) {
                delay(holdingDuration / intervals)
                // Only trigger haptic every 5 intervals to avoid overwhelming the user
                val event = EmergencyEvent.ButtonHolding(i.toFloat() / intervals)
                processEvent(event)
            }
            startCountdown()
        }
    }

    fun triggerInstantSos() {
        if (emergencyState.value !is EmergencyState.Idle) return
        processEvent(EmergencyEvent.InstantSosTriggered)
        startCountdown()
    }

    private fun startCountdown() {
        holdingJob?.cancel()
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 5 downTo 1) {
                processEvent(EmergencyEvent.CountdownTicked(i))
                delay(1000)
            }
            processEvent(EmergencyEvent.EmergencyActivated)
            startSimulationPipeline()
        }
    }

    fun cancelSos() {
        holdingJob?.cancel()
        countdownJob?.cancel()
        simulationJob?.cancel()
        processEvent(EmergencyEvent.CountdownCancelled)
        // Auto reset to idle after a short delay for UX
        viewModelScope.launch {
            delay(2000)
            processEvent(EmergencyEvent.ResetToIdle)
        }
    }

    fun onSosButtonReleased() {
        if (emergencyState.value is EmergencyState.HoldingButton) {
            holdingJob?.cancel()
            processEvent(EmergencyEvent.ButtonReleased)
            viewModelScope.launch {
                delay(1500)
                processEvent(EmergencyEvent.ResetToIdle)
            }
        }
    }

    private fun startSimulationPipeline() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            // Activated (Showing for a while)
            delay(1000)
            
            // Activated -> Acquiring Location
            processEvent(EmergencyEvent.AcquisitionStarted)
            delay(2000)
            
            // Acquiring Location -> Location Ready
            processEvent(EmergencyEvent.LocationAcquired(0.0, 0.0))
            delay(2000)
            
            // Location Ready -> Sending Alerts
            processEvent(EmergencyEvent.AlertsSent)
            delay(2000)
            
            // Sending Alerts -> Calling Primary Contact
            processEvent(EmergencyEvent.PrimaryContactCallStarted)
            for (i in 1..6) {
                delay(1000)
                processEvent(EmergencyEvent.CallTicked(i))
            }
            
            // Calling Primary Contact -> Live Tracking
            processEvent(EmergencyEvent.LiveTrackingStarted())
            
            var elapsed = 0
            while (true) {
                delay(1000)
                elapsed++
                processEvent(EmergencyEvent.TrackingTicked(elapsed))
            }
        }
    }

    fun resolveSos() {
        isConfirmingSafe = false
        simulationJob?.cancel()
        viewModelScope.launch {
            processEvent(EmergencyEvent.EmergencyCompleted)
            delay(2000)
            processEvent(EmergencyEvent.ResetToIdle)
        }
    }

    fun startSafeConfirmation() {
        isConfirmingSafe = true
        // Shortest possible guide voice for maximum discretion
        com.kaaval.app.accessibility.HapticFeedbackManager.vibrate(com.kaaval.app.accessibility.HapticFeedbackManager.HapticPattern.CALL_STARTED)
        com.kaaval.app.accessibility.VoiceFeedbackManager.speakPriority("Confirm your safety.")
    }

    fun cancelSafeConfirmation() {
        isConfirmingSafe = false
    }

    private fun handleSimulatedActionResponse(action: EmergencyAction) {
        // In a real app, handlers would eventually call processEvent
        // For simulation, we drive most steps via startSimulationPipeline
        // but we can add specific handling here if needed.
        android.util.Log.d("EmergencyViewModel", "Simulating response for action: ${action::class.simpleName}")
    }

    fun processEvent(event: EmergencyEvent) {
        stateManager.onEvent(event)
    }

    companion object {
        fun provideFactory(
            actionDispatcher: EmergencyActionDispatcher,
            repository: com.kaaval.app.domain.repository.EmergencySessionRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EmergencyViewModel(actionDispatcher, repository)
            }
        }
    }
}
