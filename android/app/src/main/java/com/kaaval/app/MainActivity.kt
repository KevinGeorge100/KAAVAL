package com.kaaval.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.kaaval.app.accessibility.VoiceFeedbackManager
import com.kaaval.app.ai.OpenAiEmergencyAnalyzer
import com.kaaval.app.data.KaavalDatabase
import com.kaaval.app.data.KaavalRepository
import com.kaaval.app.domain.model.EmergencyContact
import com.kaaval.app.domain.model.EmergencyIncident
import com.kaaval.app.domain.model.EmergencyState
import com.kaaval.app.domain.model.MedicalProfile
import com.kaaval.app.domain.model.WearableDevice
import com.kaaval.app.service.EmergencyForegroundService
import com.kaaval.app.service.KaavalLocationManager
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val voiceFeedback = VoiceFeedbackManager
    private lateinit var hapticFeedback: HapticFeedbackManager
    private lateinit var locationManager: KaavalLocationManager
    private lateinit var sosDispatcher: SosDispatcher
    private lateinit var repository: KaavalRepository
    private lateinit var openAiAnalyzer: OpenAiEmergencyAnalyzer

    private var countdownJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()

        val db = KaavalDatabase.getDatabase(this)
        repository = KaavalRepository(db)

        voiceFeedback.initialize(this)
        hapticFeedback = HapticFeedbackManager(this)
        locationManager = KaavalLocationManager(this)
        sosDispatcher = SosDispatcher(this)
        openAiAnalyzer = OpenAiEmergencyAnalyzer(apiKey = "")

        setContent {
            KAAVALTheme {
                var selectedTab by remember { mutableStateOf(0) }
                var emergencyState by remember { mutableStateOf<EmergencyState>(EmergencyState.Idle) }

                val contacts by repository.allContacts.collectAsState(initial = emptyList())
                val medicalProfileState by repository.medicalProfile.collectAsState(initial = null)

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
                val sampleWearable = remember { WearableDevice() }

                fun startCountdown() {
                    hapticFeedback.triggerCountdownPulse()
                    voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.SOS_BUTTON_HELD, isPriority = true)

                    countdownJob?.cancel()
                    countdownJob = lifecycleScope.launch {
                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.EMERGENCY_COUNTDOWN_STARTED, isPriority = true)
                        for (i in 5 downTo 1) {
                            emergencyState = EmergencyState.Countdown(i)
                            voiceFeedback.speakPriority("Activating in $i seconds")
                            hapticFeedback.triggerCountdownPulse()
                            delay(1000)
                        }

                        // Activate SOS
                        val incidentId = "KVL-${System.currentTimeMillis() / 1000}"
                        val trackingUrl = "https://kaaval-tracking.web.app/live/$incidentId"

                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.ACQUIRING_LOCATION)
                        val loc = locationManager.getCurrentLocation()
                        if (loc != null) {
                            voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.LOCATION_ACQUIRED)
                        } else {
                            voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.ERROR_OBTAINING_LOCATION)
                        }

                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.SENDING_SMS_ALERTS)
                        sosDispatcher.dispatchEmergencyAlert(
                            contacts = contacts,
                            latitude = loc?.latitude,
                            longitude = loc?.longitude,
                            trackingUrl = trackingUrl
                        )

                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.CALLING_PRIMARY_CONTACT)

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
                        hapticFeedback.triggerSosActivePattern()
                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.EMERGENCY_ACTIVATED, isPriority = true)
                        voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.LIVE_TRACKING_STARTED)

                        emergencyState = EmergencyState.Active(
                            incidentId = incidentId,
                            timestamp = System.currentTimeMillis(),
                            latitude = loc?.latitude,
                            longitude = loc?.longitude,
                            trackingUrl = trackingUrl,
                            isPrimaryCalled = true
                        )
                    }
                }

                fun cancelSos() {
                    countdownJob?.cancel()
                    EmergencyForegroundService.stopService(this@MainActivity)
                    emergencyState = EmergencyState.Idle
                    hapticFeedback.triggerCancellationRumble()
                    voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.COUNTDOWN_CANCELLED, isPriority = true)
                }

                fun resolveSos() {
                    countdownJob?.cancel()
                    EmergencyForegroundService.stopService(this@MainActivity)
                    emergencyState = EmergencyState.Idle
                    voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.LIVE_TRACKING_ENDED)
                    voiceFeedback.announce(VoiceFeedbackManager.AnnouncementType.EMERGENCY_COMPLETED, isPriority = true)
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = HighContrastBlack) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
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
                                onClick = { selectedTab = 1 },
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
                                onClick = { selectedTab = 2 },
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
                                onClick = { selectedTab = 3 },
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
                            onTriggerSos = { startCountdown() },
                            onCancelSos = { cancelSos() },
                            onResolveSos = { resolveSos() },
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
                            modifier = Modifier.padding(innerPadding)
                        )
                        2 -> MedicalProfileScreen(
                            profile = currentProfile,
                            modifier = Modifier.padding(innerPadding)
                        )
                        3 -> WearableStatusScreen(
                            device = sampleWearable,
                            onTestTactileVibration = {
                                hapticFeedback.triggerSosActivePattern()
                                voiceFeedback.speak("Testing tactile wearable vibration feedback.")
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceFeedback.shutdown()
    }
}
