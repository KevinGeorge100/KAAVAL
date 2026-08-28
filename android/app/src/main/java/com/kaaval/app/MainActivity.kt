package com.kaaval.app

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kaaval.app.accessibility.HapticFeedbackManager
import com.kaaval.app.accessibility.VoiceCommandManager
import com.kaaval.app.accessibility.VoiceFeedbackManager
import com.kaaval.app.ai.OpenAiEmergencyAnalyzer
import com.kaaval.app.data.KaavalDatabase
import com.kaaval.app.data.KaavalRepository
import com.kaaval.app.domain.action.EmergencyActionDispatcher
import com.kaaval.app.domain.model.EmergencyContact
import com.kaaval.app.domain.model.EmergencyEvent
import com.kaaval.app.domain.model.EmergencyState
import com.kaaval.app.domain.model.MedicalProfile
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
import com.kaaval.app.ui.viewmodel.EmergencyViewModel
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
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
    
    private val actionDispatcher by lazy {
        EmergencyActionDispatcher(
            context = this,
            locationManager = locationManager,
            sosDispatcher = sosDispatcher,
            bleManager = bleManager,
            audioWitness = audioWitness,
            batteryGuardian = batteryGuardian,
            repository = repository
        )
    }

    private val viewModel: EmergencyViewModel by viewModels {
        EmergencyViewModel.provideFactory(actionDispatcher, repository)
    }

    private var lastAnnouncementTime = 0L

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
                val activeMsg = if (Locale.getDefault().language == "ml") "വോയ്‌സ് കമാൻഡുകൾ സജീവമാണ്." else "Voice commands active."
                voiceFeedback.speak(activeMsg)
            }
        }

        if (permissions[Manifest.permission.BLUETOOTH_SCAN] == true) {
            bleManager.startScan()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SPRINT 4: Enable Lock-Screen Activation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val db = KaavalDatabase.getDatabase(this)
        repository = KaavalRepository(db)

        voiceFeedback.initialize(this)
        hapticFeedback.initialize(this)
        locationManager = KaavalLocationManager(this)
        sosDispatcher = SosDispatcher(this)
        openAiAnalyzer = OpenAiEmergencyAnalyzer(apiKey = "")
        audioWitness = AudioWitnessManager(this) { audioFile ->
            // AI Analysis disabled for stability sprint.
        }
        batteryGuardian = BatteryGuardianManager(this) { level -> 
            viewModel.processEvent(EmergencyEvent.CriticalBatteryDetected(level))
        } 

        bleManager = KaavalBleManager(this) {
            viewModel.processEvent(EmergencyEvent.ButtonPressed)
            voiceFeedback.speakPriority("Hardware SOS Triggered.")
        }

        voiceCommandManager = VoiceCommandManager(this) {
            viewModel.processEvent(EmergencyEvent.ButtonPressed)
        }

        requestEmergencyPermissions()
        
        // Start scanning for the tactile wearable
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bleManager.startScan()
        }

        // GPS Check on startup
        if (!locationManager.isLocationEnabled()) {
            voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.GPS_DISABLED)
        }

        checkAccessibilityService()

        // SPRINT 4: Process Global Trigger after all infra is ready
        handleIntent(intent)

        setContent {
            KAAVALTheme {
                var selectedTab by remember { mutableStateOf(0) }
                val emergencyState by viewModel.emergencyState.collectAsState()
                var isDiscreetMode by remember { mutableStateOf(false) }

                val contacts by repository.allContacts.collectAsState(initial = emptyList())
                val medicalProfileState by repository.medicalProfile.collectAsState(initial = null)
                val wearableState by bleManager.wearableState.collectAsState()

                // SPRINT 3: State Recovery Logic (Bridge to VM if needed)
                LaunchedEffect(Unit) {
                    repository.currentEmergencyState.collect { recoveredState ->
                        if (recoveredState != null && emergencyState is EmergencyState.Idle) {
                            // Recovery logic to be unified in VM
                        }
                    }
                }

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

                // Sync TTS Language with User Preference
                LaunchedEffect(currentProfile.preferredLanguage) {
                    voiceFeedback.setLanguage(currentProfile.preferredLanguage)
                }

                LaunchedEffect(wearableState.isConnected) {
                    if (wearableState.isConnected) {
                        voiceFeedback.speak("KAAVAL wearable connected.")
                        hapticFeedback.vibrate(HapticFeedbackManager.HapticPattern.SUCCESS)
                    }
                }

                fun simulateCaregiverResponse(senderName: String = "Anjali (Sister)") {
                    voiceFeedback.speakPriority("Caregiver $senderName is responding.")
                }

                LaunchedEffect(contacts) {
                    if (contacts.isNotEmpty()) {
                        smsReceiver = SmsReplyReceiver(contacts.map { it.phoneNumber }) { sender ->
                            val contactName = contacts.find { it.phoneNumber.contains(sender.takeLast(10)) }?.name ?: sender
                            simulateCaregiverResponse(contactName)
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    // Handled in VM
                }

                Scaffold(
                    bottomBar = {
                        val isStealth = isDiscreetMode && emergencyState is EmergencyState.LiveTracking
                        if (!isStealth) {
                            NavigationBar(containerColor = HighContrastBlack) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { 
                                        selectedTab = 0 
                                        lastAnnouncementTime = System.currentTimeMillis()
                                        voiceFeedback.speakPriority("Emergency SOS Screen. The giant activation button is in the center. Hold it to start an alert.")
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
                                        lastAnnouncementTime = System.currentTimeMillis()
                                        val count = contacts.size
                                        val summary = if (count == 0) "No contacts added yet." else "You have $count emergency contacts. Swipe to hear their names."
                                        voiceFeedback.speakPriority("Emergency Contacts Screen. $summary")
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
                                        lastAnnouncementTime = System.currentTimeMillis()
                                        voiceFeedback.speakPriority("Medical Profile Screen. Your clinical details are here. Use the button at the top right to read them aloud for a first responder.")
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
                                        lastAnnouncementTime = System.currentTimeMillis()
                                        val status = if (wearableState.isConnected) "Your wearable is connected and ready." else "Your wearable is not connected."
                                        voiceFeedback.speakPriority("Wearable Status Screen. $status")
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
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        0 -> MainSosScreen(
                            emergencyState = emergencyState,
                            isDiscreetMode = isDiscreetMode,
                            isConfirmingSafe = viewModel.isConfirmingSafe,
                            onStartSafeConfirmation = { viewModel.startSafeConfirmation() },
                            onConfirmSafe = { viewModel.resolveSos() },
                            onCancelSafeConfirmation = { viewModel.cancelSafeConfirmation() },
                            onDiscreetModeChange = { 
                                isDiscreetMode = it 
                                viewModel.isDiscreetMode = it
                                if (it) voiceFeedback.speak("Discreet mode on.") else voiceFeedback.speak("Standard mode on.")
                            },
                            onTriggerSos = { viewModel.onSosButtonPressed() },
                            onTriggerInstantSos = { viewModel.triggerInstantSos() },
                            onCancelSos = { viewModel.cancelSos() },
                            onResolveSos = { viewModel.resolveSos() },
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
                            onLanguageChange = { lang ->
                                lifecycleScope.launch {
                                    val updatedProfile = currentProfile.copy(preferredLanguage = lang)
                                    repository.saveMedicalProfile(updatedProfile)
                                    // Immediate voice feedback in the new language
                                    val msg = if (lang == "ml") "മലയാളം സജ്ജമാക്കി." else "English language selected."
                                    voiceFeedback.speakPriority(msg)
                                }
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXTRA_TRIGGER_SOS", false) == true) {
            android.util.Log.i("MainActivity", "SOS Triggered via Global Intent")
            viewModel.triggerInstantSos()
            // Clear the flag to prevent re-triggering on config changes
            intent.putExtra("EXTRA_TRIGGER_SOS", false)
        }
    }

    private fun checkAccessibilityService() {
        if (!isAccessibilityServiceEnabled()) {
            android.util.Log.w("MainActivity", "Accessibility Service is NOT enabled. Global trigger unavailable.")
            // No intrusive dialog for now, as per standard clean-arch/UX rules for this sprint
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedService = ComponentName(this, com.kaaval.app.service.KaavalAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        
        android.util.Log.d("MainActivity", "Checking for Service: $expectedService")
        android.util.Log.d("MainActivity", "Enabled Services: $enabledServices")
        
        return enabledServices.contains(expectedService) || enabledServices.contains(packageName)
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
        }
    }

    override fun onStop() {
        super.onStop()
        voiceCommandManager.stopListening()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && event?.repeatCount == 0) {
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
                    viewModel.triggerInstantSos()
                }
                return true // Intercept the 3rd click to trigger SOS
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
