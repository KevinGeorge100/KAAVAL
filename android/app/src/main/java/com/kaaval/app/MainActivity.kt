package com.kaaval.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.kaaval.app.accessibility.HapticFeedbackManager
import com.kaaval.app.accessibility.VoiceCommandManager
import com.kaaval.app.accessibility.VoiceFeedbackManager
import com.kaaval.app.ai.OpenAiEmergencyAnalyzer
import com.kaaval.app.data.KaavalDatabase
import com.kaaval.app.data.KaavalRepository
import com.kaaval.app.domain.model.EmergencyContact
import com.kaaval.app.domain.model.EmergencyIncident
import com.kaaval.app.domain.model.EmergencyState
import com.kaaval.app.domain.model.MedicalProfile
import com.kaaval.app.domain.model.WearableDevice
import com.kaaval.app.service.AudioWitnessManager
import com.kaaval.app.service.BatteryGuardianManager
import com.kaaval.app.service.EmergencyForegroundService
import com.kaaval.app.service.KaavalBleManager
import com.kaaval.app.service.KaavalLocationManager
import com.kaaval.app.service.SmsReplyReceiver
import com.kaaval.app.sos.SosDispatcher
import com.kaaval.app.ui.screens.ContactsScreen
import com.kaaval.app.ui.screens.MainSosScreen
import com.kaaval.app.ui.screens.MedicalProfileScreen
import com.kaaval.app.ui.screens.WearableStatusScreen
import com.kaaval.app.ui.theme.HighContrastBlack
import com.kaaval.app.ui.theme.HighContrastYellow
import com.kaaval.app.ui.theme.KAAVALTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val voiceFeedback = VoiceFeedbackManager
    private val hapticFeedback = HapticFeedbackManager
    private lateinit var locationManager: KaavalLocationManager
    private lateinit var sosDispatcher: SosDispatcher
    private lateinit var repository: KaavalRepository
    private lateinit var openAiAnalyzer: OpenAiEmergencyAnalyzer
    private lateinit var voiceCommandManager: VoiceCommandManager
    private lateinit var bleManager: KaavalBleManager
    private lateinit var audioWitness: AudioWitnessManager
    private lateinit var batteryGuardian: BatteryGuardianManager
    private var smsReceiver: SmsReplyReceiver? = null

    private var countdownJob: Job? = null
    private val voiceTriggerFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Volume Trigger Logic
    private var volumeUpClickCount = 0
    private var lastVolumeUpTime = 0L

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            voiceFeedback.speakPriority("Warning: Some permissions were denied. Emergency features may not work correctly.")
        } else {
            // Restart voice listener if microphone was just granted
            if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
                voiceCommandManager.startListening()
                voiceFeedback.speak("Voice commands active.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = KaavalDatabase.getDatabase(this)
        repository = KaavalRepository(db)

        voiceFeedback.initialize(this)
        hapticFeedback.initialize(this)
        locationManager = KaavalLocationManager(this)
        sosDispatcher = SosDispatcher(this)
        openAiAnalyzer = OpenAiEmergencyAnalyzer(apiKey = "")
        audioWitness = AudioWitnessManager(this)
        
        batteryGuardian = BatteryGuardianManager(this) { level ->
            // Final SOS call when battery is critical
            voiceFeedback.speakPriority("Warning: Critical battery level $level percent. Sending final emergency coordinates.")
            // Trigger emergency dispatch one last time
        }

        bleManager = KaavalBleManager(this) {
            // HARDWARE TRIGGER callback from the BLE module
            lifecycleScope.launch {
                voiceTriggerFlow.emit(Unit) // Triggers the same SOS flow as Voice/Button
                voiceFeedback.speakPriority("Hardware SOS Triggered.")
            }
        }

        voiceCommandManager = VoiceCommandManager(this) {
            lifecycleScope.launch {
                voiceTriggerFlow.emit(Unit)
            }
        }

        requestEmergencyPermissions()
        
        // Start scanning for the tactile wearable
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bleManager.startScan()
        }

        setContent {
            KAAVALTheme {
                var selectedTab by remember { mutableStateOf(0) }
                var emergencyState by remember { mutableStateOf<EmergencyState>(EmergencyState.Idle) }
                var isDiscreetMode by remember { mutableStateOf(false) }
                var recoveryChecked by remember { mutableStateOf(false) }

                val contacts by repository.allContacts.collectAsState(initial = emptyList())
                val medicalProfileState by repository.medicalProfile.collectAsState(initial = null)
                val wearableState by bleManager.wearableState.collectAsState()

                val defaultProfile = remember {
                    MedicalProfile(
                        fullName = "Visually Impaired User",
                        age = 26,
                        bloodGroup = "O+",
                        allergies = "Penicillin, Dust",
                        medications = "Daily Eye Drops",
                        emergencyNotes = "Visually impaired. Guided assistance required."
                    )
                }

                val currentProfile = medicalProfileState ?: defaultProfile
                // val sampleWearable = remember { WearableDevice() }

                LaunchedEffect(wearableState.isConnected) {
                    if (wearableState.isConnected) {
                        voiceFeedback.speak("KAAVAL wearable connected.")
                        hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.SUCCESS)
                    }
                }

                fun simulateCaregiverResponse(senderName: String = "Anjali (Sister)") {
                    val currentState = emergencyState
                    if (currentState is EmergencyState.Active) {
                        val updatedState = currentState.copy(respondingCaregiver = senderName)
                        emergencyState = updatedState
                        lifecycleScope.launch { repository.saveEmergencyState(updatedState) }
                        hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.CAREGIVER_RESPONDING)
                        voiceFeedback.speakPriority("Caregiver $senderName is responding.")
                    }
                }

                LaunchedEffect(contacts) {
                    if (contacts.isNotEmpty()) {
                        smsReceiver = SmsReplyReceiver(contacts.map { it.phoneNumber }) { sender ->
                            // Find the name of the contact who replied
                            val contactName = contacts.find { it.phoneNumber.contains(sender.takeLast(10)) }?.name ?: sender
                            simulateCaregiverResponse(contactName)
                        }
                    }
                }

                fun startCountdown() {
                    if (contacts.isEmpty()) {
                        voiceFeedback.speakPriority("Error: No emergency contacts found. Please add contacts before triggering SOS.")
                        hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.ERROR)
                        return
                    }

                    hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.SOS_HOLD)
                    if (!isDiscreetMode) {
                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.SOS_BUTTON_HELD, isPriority = true)
                    }

                    countdownJob?.cancel()
                    countdownJob = lifecycleScope.launch {
                        if (!isDiscreetMode) {
                            voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.EMERGENCY_COUNTDOWN_STARTED, isPriority = true)
                            delay(1500) // CRITICAL: Wait for "Activating in" intro to finish
                        }
                        
                        for (i in 5 downTo 1) {
                            emergencyState = EmergencyState.Countdown(i)
                            if (isDiscreetMode) {
                                hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.COUNTDOWN_TICK)
                            } else {
                                voiceFeedback.speakPriority(i.toString()) // Priority for instant sync
                                hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.COUNTDOWN_TICK)
                            }
                            delay(1000)
                        }

                        // Activate SOS
                        val incidentId = "KVL-${System.currentTimeMillis() / 1000}"
                        val trackingUrl = "https://kaaval-94c1d.web.app/live/$incidentId"

                        // Start Audio Witness recording (Final Boss Feature #2)
                        audioWitness.startRecording(incidentId)
                        batteryGuardian.startMonitoring() // Final Boss Feature #3

                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.ACQUIRING_LOCATION)
                        val loc = locationManager.getCurrentLocation()
                        if (loc != null) {
                            hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.LOCATION_ACQUIRED)
                            voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.LOCATION_ACQUIRED)
                        } else {
                            hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.ERROR)
                            voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.ERROR_OBTAINING_LOCATION)
                        }

                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.SENDING_SMS_ALERTS)
                        sosDispatcher.dispatchEmergencyAlert(
                            contacts = contacts,
                            latitude = loc?.latitude,
                            longitude = loc?.longitude,
                            trackingUrl = trackingUrl
                        )
                        hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.SMS_SENT)

                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.CALLING_PRIMARY_CONTACT)
                        hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.CALL_STARTED)

                        val incident = EmergencyIncident(
                            incidentId = incidentId,
                            timestamp = System.currentTimeMillis(),
                            latitude = loc?.latitude,
                            longitude = loc?.longitude,
                            status = "ACTIVE",
                            trackingUrl = trackingUrl
                        )
                        repository.logIncident(incident)

                        EmergencyForegroundService.startService(this@MainActivity)
                        hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.SOS_ACTIVATED)
                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.EMERGENCY_ACTIVATED, isPriority = true)
                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.LIVE_TRACKING_STARTED)
                        hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.LIVE_TRACKING_STARTED)

                        val activeState = EmergencyState.Active(
                            incidentId = incidentId,
                            timestamp = System.currentTimeMillis(),
                            latitude = loc?.latitude,
                            longitude = loc?.longitude,
                            trackingUrl = trackingUrl,
                            isPrimaryCalled = true
                        )
                        repository.saveEmergencyState(activeState)
                        emergencyState = activeState
                    }
                }

                LaunchedEffect(recoveryChecked) {
                    if (recoveryChecked) return@LaunchedEffect
                    val persistedState = repository.getEmergencyState()
                    if (persistedState != null && emergencyState is EmergencyState.Idle) {
                        emergencyState = persistedState
                        EmergencyForegroundService.startService(this@MainActivity)
                        hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.LIVE_TRACKING_STARTED)
                        voiceFeedback.speakPriority("Recovered active emergency session after restart.")
                    }
                    recoveryChecked = true
                }

                LaunchedEffect(Unit) {
                    voiceTriggerFlow.collect {
                        if (emergencyState is EmergencyState.Idle) {
                            startCountdown()
                        }
                    }
                }

                fun cancelSos() {
                    countdownJob?.cancel()
                    EmergencyForegroundService.stopService(this@MainActivity)
                    audioWitness.stopRecording()
                    batteryGuardian.stopMonitoring()
                    lifecycleScope.launch { repository.clearEmergencyState() }
                    emergencyState = EmergencyState.Idle
                    hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.COUNTDOWN_CANCELLED)
                    voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.COUNTDOWN_CANCELLED, isPriority = true)
                }

                fun resolveSos() {
                    countdownJob?.cancel()
                    EmergencyForegroundService.stopService(this@MainActivity)
                    audioWitness.stopRecording()
                    batteryGuardian.stopMonitoring()
                    lifecycleScope.launch { repository.clearEmergencyState() }
                    emergencyState = EmergencyState.Idle
                    hapticFeedback.cancel() // Stop the heartbeat
                    hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.SUCCESS)
                    voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.LIVE_TRACKING_ENDED)
                    voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.EMERGENCY_COMPLETED, isPriority = true)
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = HighContrastBlack) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { 
                                    selectedTab = 0 
                                    voiceFeedback.speak("Emergency SOS Screen")
                                },
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("SOS", fontSize = 12.sp, color = HighContrastYellow) },
                                modifier = Modifier.semantics {
                                    role = Role.Tab
                                    contentDescription = "Emergency SOS Screen Tab"
                                    stateDescription = if (selectedTab == 0) "Selected. Tab 1 of 4" else "Not selected. Tab 1 of 4"
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = HighContrastBlack,
                                    indicatorColor = HighContrastYellow
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { 
                                    selectedTab = 1 
                                    voiceFeedback.speak("Emergency Contacts Screen")
                                },
                                icon = { Icon(Icons.Default.People, contentDescription = null) },
                                label = { Text("Contacts", fontSize = 12.sp, color = HighContrastYellow) },
                                modifier = Modifier.semantics {
                                    role = Role.Tab
                                    contentDescription = "Emergency Contacts Screen Tab"
                                    stateDescription = if (selectedTab == 1) "Selected. Tab 2 of 4" else "Not selected. Tab 2 of 4"
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = HighContrastBlack,
                                    indicatorColor = HighContrastYellow
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { 
                                    selectedTab = 2 
                                    voiceFeedback.speak("Medical Profile Screen")
                                },
                                icon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                                label = { Text("Medical", fontSize = 12.sp, color = HighContrastYellow) },
                                modifier = Modifier.semantics {
                                    role = Role.Tab
                                    contentDescription = "Medical Profile Screen Tab"
                                    stateDescription = if (selectedTab == 2) "Selected. Tab 3 of 4" else "Not selected. Tab 3 of 4"
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = HighContrastBlack,
                                    indicatorColor = HighContrastYellow
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { 
                                    selectedTab = 3 
                                    voiceFeedback.speak("Wearable Status Screen")
                                },
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text("Wearable", fontSize = 12.sp, color = HighContrastYellow) },
                                modifier = Modifier.semantics {
                                    role = Role.Tab
                                    contentDescription = "BLE Wearable Status Tab"
                                    stateDescription = if (selectedTab == 3) "Selected. Tab 4 of 4" else "Not selected. Tab 4 of 4"
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = HighContrastBlack,
                                    indicatorColor = HighContrastYellow
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        0 -> MainSosScreen(
                            emergencyState = emergencyState,
                            isDiscreetMode = isDiscreetMode,
                            onDiscreetModeChange = { 
                                isDiscreetMode = it 
                                if (it) voiceFeedback.speak("Discreet mode on.") else voiceFeedback.speak("Standard mode on.")
                            },
                            onTriggerSos = { startCountdown() },
                            onCancelSos = { cancelSos() },
                            onResolveSos = { resolveSos() },
                            onSimulateCaregiverResponse = { simulateCaregiverResponse() },
                            onSimulateCrash = { throw RuntimeException("Simulated crash for emergency recovery testing") },
                            modifier = Modifier.padding(innerPadding)
                        )
                        1 -> ContactsScreen(
                            contacts = contacts,
                            onAddContact = { name, phone, rel ->
                                lifecycleScope.launch {
                                    repository.insertContact(
                                        EmergencyContact(
                                            id = System.currentTimeMillis().toString(),
                                            name = name,
                                            phoneNumber = phone,
                                            relationship = rel,
                                            isPrimary = contacts.isEmpty()
                                        )
                                    )
                                    voiceFeedback.speak("Added contact $name")
                                }
                            },
                            onDeleteContact = { contact ->
                                lifecycleScope.launch {
                                    repository.deleteContact(contact)
                                    voiceFeedback.speak("Removed contact ${contact.name}")
                                }
                            },
                            onSetPrimary = { contact ->
                                lifecycleScope.launch {
                                    repository.setPrimaryContact(contact.id)
                                    voiceFeedback.speak("${contact.name} is now your primary contact.")
                                }
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                        2 -> MedicalProfileScreen(
                            profile = currentProfile,
                            onReadProfileAloud = {
                                val text = "Emergency Medical Profile for ${currentProfile.fullName}. " +
                                        "Blood group ${currentProfile.bloodGroup}. " +
                                        "Allergies: ${currentProfile.allergies}. " +
                                        "Medications: ${currentProfile.medications}. " +
                                        "Instructions: ${currentProfile.emergencyNotes}"
                                voiceFeedback.speak(text)
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                        3 -> WearableStatusScreen(
                            device = wearableState,
                            onTestTactileVibration = {
                                hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.SOS_ACTIVATED)
                                voiceFeedback.speak("Testing tactile wearable vibration feedback.")
                            },
                            onRefreshScan = {
                                bleManager.startScan()
                                voiceFeedback.speak("Scanning for KAAVAL wearable.")
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    private fun requestEmergencyPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // Deprecated but required for older APIs
            @Suppress("DEPRECATION")
            permissions.add(Manifest.permission.BLUETOOTH)
            @Suppress("DEPRECATION")
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            voiceCommandManager.startListening()
            // Optional: voiceFeedback.speak("Voice commands active.") 
            // Better to keep it quiet on every start, but good for testing.
        }
    }

    override fun onStop() {
        super.onStop()
        voiceCommandManager.stopListening()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastVolumeUpTime < 1500) {
                volumeUpClickCount++
            } else {
                volumeUpClickCount = 1
            }
            lastVolumeUpTime = currentTime

            if (volumeUpClickCount >= 3) {
                volumeUpClickCount = 0
                lifecycleScope.launch {
                    voiceTriggerFlow.emit(Unit)
                    voiceFeedback.speakPriority("Tactile SOS Triggered via buttons.")
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceFeedback.shutdown()
        hapticFeedback.shutdown()
    }
}
