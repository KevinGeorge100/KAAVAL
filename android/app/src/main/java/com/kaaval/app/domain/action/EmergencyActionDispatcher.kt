package com.kaaval.app.domain.action

import android.content.Context
import android.util.Log
import com.kaaval.app.domain.action.handlers.AnalyticsActionHandler
import com.kaaval.app.domain.action.handlers.HapticActionHandler
import com.kaaval.app.domain.action.handlers.LocationActionHandler
import com.kaaval.app.domain.action.handlers.SmsActionHandler
import com.kaaval.app.domain.action.handlers.VoiceActionHandler
import com.kaaval.app.accessibility.HapticFeedbackManager
import com.kaaval.app.accessibility.HapticFeedbackManager.HapticPattern
import com.kaaval.app.accessibility.VoiceFeedbackManager
import com.kaaval.app.service.EmergencyForegroundService
import com.kaaval.app.domain.model.EmergencyAction
import com.kaaval.app.domain.model.EmergencyEvent
import com.kaaval.app.domain.model.EmergencySession
import com.kaaval.app.domain.model.SessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * KAAVAL Emergency Action Dispatcher
 * The execution layer that bridges the State Machine with specialized Handlers.
 * Responsibilities:
 * - Receive actions from the ViewModel
 * - Coordinate delegation to specialized Handlers
 */
class EmergencyActionDispatcher(
    private val context: Context,
    private val locationManager: com.kaaval.app.service.KaavalLocationManager,
    private val sosDispatcher: com.kaaval.app.sos.SosDispatcher,
    private val audioWitness: com.kaaval.app.service.AudioWitnessManager,
    private val batteryGuardian: com.kaaval.app.service.BatteryGuardianManager,
    private val repository: com.kaaval.app.data.KaavalRepository
) {

    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
    private val locationHandler = LocationActionHandler(locationManager, repository)
    private val smsHandler = SmsActionHandler(sosDispatcher, repository)
    private val voiceHandler = VoiceActionHandler()
    private val hapticHandler = HapticActionHandler()
    private val analyticsHandler = AnalyticsActionHandler()

    var isDiscreetMode: Boolean = false

    /**
     * Delegates an action to its corresponding handler.
     */
    fun dispatch(action: EmergencyAction, onActionHandled: (EmergencyEvent) -> Unit = {}) {
        // Filter logic:
        // 1. In Discreet Mode, we SILENCE all voice announcements for stealth & safety.
        // 2. In Standard Mode, we silence only intermediate confirmations (Location, SMS)
        //    to keep the experience professional and less "chatty", using Haptics instead.
        if (action is EmergencyAction.PlayVoiceAnnouncement) {
            if (isDiscreetMode) {
                Log.d("ActionDispatcher", "Discreet mode active. Silencing announcement ${action.announcementType}")
                return
            }
            val intermediateAnnouncements = listOf("LOCATION_ACQUIRED", "SENDING_SMS_ALERTS")
            if (!action.isPriority && intermediateAnnouncements.contains(action.announcementType)) {
                Log.d("ActionDispatcher", "Silencing intermediate announcement ${action.announcementType}. Using Haptic confirm.")
                HapticFeedbackManager.vibrate(HapticPattern.SUCCESS)
                return
            }
        }

        Log.d("ActionDispatcher", "Dispatching action to handler: ${action::class.simpleName}")
        
        when (action) {
            is EmergencyAction.CompositeAction -> {
                action.actions.forEach { dispatch(it, onActionHandled) }
            }
            is EmergencyAction.AcquireLocation -> {
                locationHandler.handle(action, onActionHandled)
            }
            is EmergencyAction.SendSmsAlerts -> {
                smsHandler.handle(action, onActionHandled)
            }
            is EmergencyAction.PlayVoiceAnnouncement -> {
                voiceHandler.handle(action)
            }
            is EmergencyAction.PlayHapticPattern -> {
                hapticHandler.handle(action)
            }
            is EmergencyAction.StartSession -> {
                scope.launch {
                    val session = EmergencySession(
                        incidentId = action.incidentId,
                        startedAt = System.currentTimeMillis(),
                        status = SessionStatus.ACTIVE
                    )
                    val success = repository.createSession(session)
                    if (success) {
                        EmergencyForegroundService.startService(context, action.incidentId)
                    }
                }
            }
            is EmergencyAction.StartAudioWitness -> {
                // Add a small delay to avoid recording the "Emergency Activated" TTS announcement
                scope.launch {
                    kotlinx.coroutines.delay(1500.milliseconds)
                    audioWitness.startRecording(action.incidentId)
                }
            }
            is EmergencyAction.StopAudioWitness -> {
                audioWitness.stopRecording()
            }
            is EmergencyAction.StartBatteryMonitoring -> {
                batteryGuardian.startMonitoring()
            }
            is EmergencyAction.StopBatteryMonitoring -> {
                batteryGuardian.stopMonitoring()
            }
            is EmergencyAction.StartLiveTracking -> {
                // Handled in Activity context for Foreground Service usually,
                // but can be bridged here if needed.
            }
            is EmergencyAction.CallPrimaryContact -> {
                // Specific name-based announcement handled inside handleCall
                smsHandler.handleCall(onActionHandled)
            }
            is EmergencyAction.CompleteEmergency -> {
                scope.launch {
                    val active = repository.getActiveSession().first()
                    active?.let {
                        repository.completeSession(it.incidentId)
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            com.kaaval.app.data.repository.FirebaseTrackingRepository().completeTrackingSession(it.incidentId)
                        }
                    }
                    EmergencyForegroundService.stopService(context)
                }
                audioWitness.stopRecording()
                batteryGuardian.stopMonitoring()
                HapticFeedbackManager.cancel()
            }
            is EmergencyAction.CancelCountdown -> {
                scope.launch {
                    val active = repository.getActiveSession().first()
                    active?.let {
                        repository.completeSession(it.incidentId)
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            com.kaaval.app.data.repository.FirebaseTrackingRepository().completeTrackingSession(it.incidentId)
                        }
                    }
                    EmergencyForegroundService.stopService(context)
                }
                audioWitness.stopRecording()
                batteryGuardian.stopMonitoring()
                HapticFeedbackManager.cancel()
                VoiceFeedbackManager.stop()
            }
            is EmergencyAction.LogAnalytics -> {
                analyticsHandler.handle(action)
            }
            else -> {
                Log.d("ActionDispatcher", "Delegation for ${action::class.simpleName} pending or internal.")
            }
        }
    }
}
