package com.kaaval.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaval.app.domain.model.EmergencyState
import com.kaaval.app.ui.theme.ActiveGreen
import com.kaaval.app.ui.theme.EmergencyRed
import com.kaaval.app.ui.theme.HighContrastBlack
import com.kaaval.app.ui.theme.HighContrastYellow
import com.kaaval.app.ui.theme.HighContrastBlack
import com.kaaval.app.ui.theme.HighContrastYellow
import com.kaaval.app.ui.theme.HighContrastBlack
import com.kaaval.app.ui.theme.HighContrastYellow

/**
 * Main SOS Emergency Screen
 * Fully accessibility-hardened with Jetpack Compose Semantics for Android TalkBack.
 */
@Composable
fun MainSosScreen(
    emergencyState: EmergencyState,
    isDiscreetMode: Boolean,
    onDiscreetModeChange: (Boolean) -> Unit,
    onTriggerSos: () -> Unit,
    onCancelSos: () -> Unit,
    onResolveSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSafeConfirmation by remember { mutableStateOf(false) }
    
    // Outermost container handles the "Anywhere on screen" P-Gesture
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HighContrastBlack)
            .pointerInput(Unit) {
                val path = mutableListOf<Offset>()
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    path.clear()
                    path.add(down.position)
                    
                    drag(down.id) { change ->
                        path.add(change.position)
                        change.consume()
                    }
                    
                    if (isStrictPShape(path)) {
                        onTriggerSos()
                    }
                }
            }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.semantics {
                    contentDescription = "KAAVAL Emergency SOS System Header"
                    stateDescription = "Accessibility Emergency Ecosystem Active"
                }
            ) {
                Text(
                    text = "KAAVAL SOS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = HighContrastYellow
                )
                Text(
                    text = "Emergency Response Ecosystem",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            // Discreet Mode Toggle (Student Persona Enhancement)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = isDiscreetMode,
                    onCheckedChange = onDiscreetModeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HighContrastYellow,
                        checkedTrackColor = HighContrastYellow.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.semantics {
                        contentDescription = "Discreet SOS Mode Toggle"
                        stateDescription = if (isDiscreetMode) "Discreet mode enabled. Countdown will be silent." else "Standard mode enabled. Voice feedback active."
                    }
                )
                Text("DISCREET", fontSize = 10.sp, color = HighContrastYellow, fontWeight = FontWeight.Bold)
            }
        }

        // Center Content
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (emergencyState) {
                is EmergencyState.Idle -> {
                    // Giant Accessible Tactile SOS Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(240.dp)
                            .clip(CircleShape)
                            .background(EmergencyRed)
                            .border(6.dp, HighContrastYellow, CircleShape)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Emergency SOS Button"
                                stateDescription = "Ready. Double tap or press and hold to trigger emergency alert."
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { onTriggerSos() },
                                    onTap = { onTriggerSos() }
                                )
                            }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SOS",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "TAP TO ALERT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighContrastYellow
                            )
                        }
                    }
                }

                is EmergencyState.Countdown -> {
                    // Countdown State
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.semantics {
                            contentDescription = "Emergency Activation Countdown Timer"
                            stateDescription = "Activating emergency alert in ${emergencyState.secondsRemaining} seconds. Double tap cancel emergency button below to stop."
                        }
                    ) {
                        Text(
                            text = "ACTIVATING IN",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighContrastYellow
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                                .background(HighContrastYellow)
                        ) {
                            Text(
                                text = "${emergencyState.secondsRemaining}",
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Black,
                                color = HighContrastBlack
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onCancelSos,
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Cancel Emergency Alert Button"
                                    stateDescription = "Double tap to stop countdown and cancel alert"
                                },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "CANCEL EMERGENCY",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                is EmergencyState.Active -> {
                    // Active Emergency State
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.semantics {
                            contentDescription = "Live Caregiver Tracking Emergency Incident Status Card"
                            stateDescription = "Emergency Active. Incident ID: ${emergencyState.incidentId}. SMS sent to emergency contacts and primary contact call initiated. Live GPS location is actively being shared with caregivers."
                        }
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2433)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = ActiveGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "EMERGENCY ACTIVE",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ActiveGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Coordination Feedback (Differentiator #2)
                                if (emergencyState.respondingCaregiver != null) {
                                    Text(
                                        text = "${emergencyState.respondingCaregiver} IS RESPONDING",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ActiveGreen,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Tactile heartbeat assurance active.",
                                        fontSize = 12.sp,
                                        color = Color.LightGray
                                    )
                                } else {
                                    Text(
                                        text = "SMS sent to emergency contacts.\nWaiting for caregiver acknowledgement...",
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Live Tracking ID: ${emergencyState.incidentId}",
                                    fontSize = 12.sp,
                                    color = HighContrastYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showSafeConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Mark Self Safe and Resolve Emergency Button"
                                    stateDescription = "Double tap to open confirmation dialog to mark yourself safe"
                                },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "I AM SAFE NOW",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = HighContrastBlack
                            )
                        }
                    }
                }

                is EmergencyState.Cancelled, EmergencyState.Resolved -> {
                    Text(
                        text = "System Ready",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.semantics {
                            contentDescription = "System Ready"
                            stateDescription = "No active emergency. System monitoring."
                        }
                    )
                }
            }
        }

        // Bottom Accessibility Guidance
        Text(
            text = "Voice Commands & TalkBack Enabled",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.semantics {
                contentDescription = "Accessibility Guidance Footer"
                stateDescription = "Voice commands and TalkBack screen reader support active"
            }
        )
    }

    // CRITICAL FIX #4: Confirmation for "I AM SAFE NOW"
    if (showSafeConfirmation) {
        AlertDialog(
            onDismissRequest = { showSafeConfirmation = false },
            title = { Text("Are you sure you are safe?", fontWeight = FontWeight.Bold, color = HighContrastYellow) },
            text = { Text("This will stop live tracking and inform your caregivers that you are safe.", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        showSafeConfirmation = false
                        onResolveSos()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen)
                ) {
                    Text("YES, I AM SAFE", color = HighContrastBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSafeConfirmation = false }) {
                    Text("NOT YET", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E2433)
        )
    }
}

/**
 * Recognizes a strict UPPERCASE "P" drawn in a single continuous stroke.
 * Order: Downstroke -> Upstroke -> Right Curve -> Back to Stem.
 */
private fun isStrictPShape(path: List<Offset>): Boolean {
    if (path.size < 20) return false

    val start = path[0]
    var index = 0
    
    // 1. Mandatory Downstroke (Minimum 250px)
    var maxDownY = start.y
    var foundDownstroke = false
    while (index < path.size) {
        if (path[index].y > maxDownY) maxDownY = path[index].y
        if (maxDownY - start.y > 250f) foundDownstroke = true
        // If we start moving back up significantly, end this phase
        if (foundDownstroke && path[index].y < maxDownY - 40f) break
        index++
    }
    if (!foundDownstroke) return false

    // 2. Return to Top (Move back up towards the start)
    var backAtTop = false
    while (index < path.size) {
        if (path[index].y < start.y + 120f) {
            backAtTop = true
            break
        }
        index++
    }
    if (!backAtTop) return false

    // 3. Rightward Curve (The top of the P)
    var pushedRight = false
    var curvedDown = false
    var closedToStem = false
    val loopStartX = path[index].x
    val loopStartY = path[index].y

    while (index < path.size) {
        val p = path[index]
        if (p.x > loopStartX + 120f) pushedRight = true
        if (pushedRight && p.y > loopStartY + 100f) curvedDown = true
        // Loop back left towards the vertical stem
        if (curvedDown && p.x < loopStartX + 80f) closedToStem = true
        index++
    }

    return pushedRight && curvedDown && closedToStem
}
