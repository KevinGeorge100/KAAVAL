package com.kaaval.app.domain

import android.util.Log
import com.kaaval.app.domain.model.EmergencyAction
import com.kaaval.app.domain.model.EmergencyEvent
import com.kaaval.app.domain.model.EmergencyState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * KAAVAL Emergency State Manager
 * Central source of truth for the emergency lifecycle.
 * Manages valid state transitions and emits execution actions.
 */
class EmergencyStateManager(private val logger: (String, String) -> Unit = { tag, msg -> Log.d(tag, msg) }) {

    private val _state = MutableStateFlow<EmergencyState>(EmergencyState.Idle)
    val state: StateFlow<EmergencyState> = _state.asStateFlow()

    private val _action = MutableSharedFlow<EmergencyAction>(extraBufferCapacity = 1)
    val action: SharedFlow<EmergencyAction> = _action.asSharedFlow()

    private var currentIncidentId: String? = null

    fun onEvent(event: EmergencyEvent) {
        val currentState = _state.value
        logger("StateManager", "Transition: ${currentState::class.simpleName} -> Event: ${event::class.simpleName}")
        
        // Define transition and action logic
        val transition = when (event) {
            is EmergencyEvent.ButtonPressed -> {
                if (currentState is EmergencyState.Idle) {
                    currentIncidentId = "KVL-${System.currentTimeMillis() / 1000}"
                    Pair(EmergencyState.HoldingButton(0f), EmergencyAction.PlayVoiceAnnouncement("SOS_BUTTON_HELD", isPriority = true))
                } else null
            }
            is EmergencyEvent.ButtonHolding -> {
                if (currentState is EmergencyState.HoldingButton) {
                    val shouldVibrate = ((event.progress * 100).toInt() % 20 == 0)
                    Pair(
                        EmergencyState.HoldingButton(event.progress),
                        if (shouldVibrate) EmergencyAction.PlayHapticPattern("SOS_HOLD") else null,
                    )
                } else null
            }
            is EmergencyEvent.ButtonReleased -> {
                if (currentState is EmergencyState.HoldingButton) {
                    currentIncidentId = null
                    Pair(
                        EmergencyState.Idle,
                        EmergencyAction.CompositeAction(
                            listOf(
                                EmergencyAction.CancelCountdown,
                                EmergencyAction.PlayVoiceAnnouncement("COUNTDOWN_CANCELLED", isPriority = true),
                                EmergencyAction.PlayHapticPattern("COUNTDOWN_CANCELLED")
                            )
                        )
                    )
                } else null
            }
            is EmergencyEvent.CountdownTicked -> {
                if (currentState is EmergencyState.HoldingButton || currentState is EmergencyState.Countdown || currentState is EmergencyState.Idle) {
                    // Ensure incident ID exists for instant triggers
                    if (currentIncidentId == null) {
                        currentIncidentId = "KVL-${System.currentTimeMillis() / 1000}"
                    }

                    val actions = mutableListOf(
                        EmergencyAction.PlayHapticPattern("COUNTDOWN_TICK"),
                        EmergencyAction.PlayVoiceAnnouncement(event.secondsRemaining.toString(), isPriority = true)
                    )
                    
                    // Add "StartSession" if we just started the countdown
                    if (currentState !is EmergencyState.Countdown && event.secondsRemaining == 5) {
                        actions.add(1, EmergencyAction.StartSession(currentIncidentId!!))
                    }

                    Pair(
                        EmergencyState.Countdown(event.secondsRemaining),
                        EmergencyAction.CompositeAction(actions)
                    )
                } else null
            }
            is EmergencyEvent.InstantSosTriggered -> {
                if (currentState is EmergencyState.Idle) {
                    currentIncidentId = "KVL-${System.currentTimeMillis() / 1000}"
                    Pair(EmergencyState.Idle, null) // Just a trigger, first tick will handle voice/state/session
                } else null
            }
            is EmergencyEvent.CountdownFinished, EmergencyEvent.EmergencyActivated -> {
                if (currentState is EmergencyState.Countdown || currentState is EmergencyState.HoldingButton) {
                    val incidentId = currentIncidentId ?: "KVL-${System.currentTimeMillis() / 1000}"
                    Pair(
                        EmergencyState.Activated,
                        EmergencyAction.CompositeAction(
                            listOf(
                                EmergencyAction.PlayVoiceAnnouncement("EMERGENCY_ACTIVATED", isPriority = true),
                                EmergencyAction.StartAudioWitness(incidentId),
                                EmergencyAction.StartBatteryMonitoring,
                                EmergencyAction.AcquireLocation
                            )
                        )
                    )
                } else null
            }
            is EmergencyEvent.CountdownCancelled -> {
                if (currentState is EmergencyState.Countdown) {
                    currentIncidentId = null
                    Pair(
                        EmergencyState.Idle,
                        EmergencyAction.CompositeAction(
                            listOf(
                                EmergencyAction.CancelCountdown,
                                EmergencyAction.PlayVoiceAnnouncement("COUNTDOWN_CANCELLED", isPriority = true),
                                EmergencyAction.PlayHapticPattern("COUNTDOWN_CANCELLED")
                            )
                        )
                    )
                } else null
            }
            is EmergencyEvent.AcquisitionStarted -> {
                if (currentState is EmergencyState.Activated) {
                    Pair(EmergencyState.AcquiringLocation, EmergencyAction.AcquireLocation)
                } else null
            }
            is EmergencyEvent.LocationAcquired -> {
                if (currentState is EmergencyState.AcquiringLocation || currentState is EmergencyState.Activated) {
                    Pair(
                        EmergencyState.LocationReady(event.latitude, event.longitude),
                        EmergencyAction.CompositeAction(
                            listOf(
                                EmergencyAction.PlayVoiceAnnouncement("LOCATION_ACQUIRED"),
                                EmergencyAction.SendSmsAlerts(event.latitude, event.longitude)
                            )
                        )
                    )
                } else null
            }
            is EmergencyEvent.LocationFailed -> {
                if (currentState is EmergencyState.AcquiringLocation) {
                    Pair(EmergencyState.Error("Location acquisition failed"), EmergencyAction.PlayVoiceAnnouncement("ERROR_OBTAINING_LOCATION"))
                } else null
            }
            is EmergencyEvent.AlertsSent -> {
                if (currentState is EmergencyState.LocationReady || currentState is EmergencyState.AcquiringLocation) {
                    Pair(
                        EmergencyState.SendingAlerts,
                        EmergencyAction.CompositeAction(
                            listOf(
                                EmergencyAction.PlayVoiceAnnouncement("SENDING_SMS_ALERTS"),
                                EmergencyAction.CallPrimaryContact
                            )
                        )
                    )
                } else null
            }
            is EmergencyEvent.PrimaryContactCallStarted -> {
                if (currentState is EmergencyState.SendingAlerts || currentState is EmergencyState.LocationReady) {
                    Pair(EmergencyState.CallingPrimaryContact(0), EmergencyAction.CallPrimaryContact)
                } else null
            }
            is EmergencyEvent.LiveTrackingStarted -> {
                if (currentState is EmergencyState.CallingPrimaryContact || currentState is EmergencyState.SendingAlerts || currentState is EmergencyState.LocationReady) {
                    val incidentId = currentIncidentId ?: "KVL-${System.currentTimeMillis() / 1000}"
                    Pair(
                        EmergencyState.LiveTracking(
                            incidentId = incidentId,
                            timestamp = System.currentTimeMillis(),
                            latitude = event.latitude,
                            longitude = event.longitude
                        ),
                        EmergencyAction.StartLiveTracking
                    )
                } else null
            }
            is EmergencyEvent.CallTicked -> {
                if (currentState is EmergencyState.CallingPrimaryContact) {
                    Pair(EmergencyState.CallingPrimaryContact(event.durationSeconds), null)
                } else null
            }
            is EmergencyEvent.TrackingTicked -> {
                if (currentState is EmergencyState.LiveTracking) {
                    Pair(currentState.copy(elapsedTimeSeconds = event.elapsedTimeSeconds), null)
                } else null
            }
            is EmergencyEvent.EmergencyCompleted -> {
                currentIncidentId = null
                Pair(EmergencyState.Idle, EmergencyAction.CompleteEmergency)
            }
            is EmergencyEvent.ErrorOccurred -> {
                Pair(EmergencyState.Error(event.reason), EmergencyAction.PlayVoiceAnnouncement("ERROR"))
            }
            is EmergencyEvent.CriticalBatteryDetected -> {
                val liveState = currentState as? EmergencyState.LiveTracking
                if (liveState != null) {
                    Pair(
                        currentState, // Stay in live tracking
                        EmergencyAction.CompositeAction(
                            listOf(
                                EmergencyAction.PlayVoiceAnnouncement("BATTERY_LOW", isPriority = true),
                                EmergencyAction.SendSmsAlerts(
                                    latitude = liveState.latitude,
                                    longitude = liveState.longitude,
                                    alertType = EmergencyAction.AlertType.BATTERY_CRITICAL
                                )
                            )
                        )
                    )
                } else null
            }
            is EmergencyEvent.SessionRecovered -> {
                if (currentState is EmergencyState.Idle || currentState is EmergencyState.LiveTracking) {
                    currentIncidentId = event.incidentId
                    logger("StateManager", "STATE_RESTORED incidentId=${event.incidentId}")
                    Pair(
                        EmergencyState.LiveTracking(
                            incidentId = event.incidentId,
                            timestamp = System.currentTimeMillis(),
                            latitude = event.latitude,
                            longitude = event.longitude
                        ),
                        null // No side effects for recovery, just restore state
                    )
                } else null
            }
            is EmergencyEvent.ResetToIdle -> {
                currentIncidentId = null
                Pair(EmergencyState.Idle, EmergencyAction.ResetState)
            }
        }

        // Execute transition and emit action
        transition?.let { (nextState, nextAction) ->
            _state.value = nextState
            logger("StateManager", "New State: ${nextState::class.simpleName}")
            nextAction?.let { 
                logger("StateManager", "Emitting Action: ${nextAction::class.simpleName}")
                _action.tryEmit(it) 
            }
        }
    }
}
