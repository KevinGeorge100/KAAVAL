package com.kaaval.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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

/**
 * Main SOS Emergency Screen
 * Fully accessibility-hardened with Jetpack Compose Semantics for Android TalkBack.
 */
@Composable
fun MainSosScreen(
    emergencyState: EmergencyState,
    isDiscreetMode: Boolean,
    isConfirmingSafe: Boolean,
    onStartSafeConfirmation: () -> Unit,
    onConfirmSafe: () -> Unit,
    onCancelSafeConfirmation: () -> Unit,
    onDiscreetModeChange: (Boolean) -> Unit,
    onTriggerSos: () -> Unit,
    onTriggerInstantSos: () -> Unit,
    onCancelSos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Outermost container
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HighContrastBlack)
                .pointerInput(emergencyState) {
                    val path = mutableListOf<Offset>()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        path.clear()
                        path.add(down.position)
                        
                        drag(down.id) { change ->
                            path.add(change.position)
                            change.consume()
                        }
                        
                        if (emergencyState is EmergencyState.Idle && isStrictPShape(path)) {
                            onTriggerInstantSos()
                        }
                    }
                }
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Header Title (Hidden in Stealth)
            val isStealth = isDiscreetMode && emergencyState is EmergencyState.LiveTracking
            
            if (!isStealth) {
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
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HighContrastYellow,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "ONE PRESS. INSTANT PROTECTION.",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }

                    // Discreet Mode Toggle
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
            }

            // Center Content
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (emergencyState) {
                    is EmergencyState.Idle, is EmergencyState.HoldingButton -> {
                        // Giant Accessible Tactile SOS Button
                        val progress = (emergencyState as? EmergencyState.HoldingButton)?.progress ?: 0f
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                                .background(if (progress > 0f) EmergencyRed.copy(alpha = 0.7f) else EmergencyRed)
                                .border(6.dp, HighContrastYellow, CircleShape)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Emergency SOS Button"
                                    stateDescription = if (progress > 0f) "Triggering SOS. Progress ${ (progress * 100).toInt() } percent." else "Ready. Double tap or press and hold to trigger emergency alert."
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = { onTriggerSos() },
                                    onTap = { onTriggerSos() }
                                    )
                                }
                        ) {
                            if (progress > 0f) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = HighContrastYellow,
                                    strokeWidth = 8.dp
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (progress > 0f) "HOLDING" else "SOS",
                                    fontSize = if (progress > 0f) 32.sp else 42.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = if (progress > 0f) "${ (progress * 100).toInt() }%" else "HOLD TO ACTIVATE",
                                    fontSize = 10.sp,
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
                                    text = emergencyState.secondsRemaining.toString(),
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

                    is EmergencyState.LiveTracking -> {
                        if (isDiscreetMode) {
                            // Discreet Stealth UI: Full Black Screen with double-tap safety
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                                    .pointerInput(Unit) {
                                        detectTapGestures(onDoubleTap = { onStartSafeConfirmation() })
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Minimal pulsing indicator
                                val infiniteTransition = rememberInfiniteTransition(label = "stealth")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.05f,
                                    targetValue = 0.2f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2000),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "heartbeat"
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ActiveGreen.copy(alpha = alpha),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        } else {
                            // Standard Active Emergency UI
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
                                    onClick = { onStartSafeConfirmation() },
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
                    }

                    is EmergencyState.Cancelled, EmergencyState.Resolved, EmergencyState.Completed -> {
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

                    is EmergencyState.Error -> {
                        Text(
                            text = "Error: ${emergencyState.reason}",
                            color = EmergencyRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    else -> {
                        CircularProgressIndicator(color = HighContrastYellow)
                    }
                }
            }

            // Bottom Accessibility Guidance (Hidden in Stealth)
            if (!isStealth) {
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
        }

        // FULL SCREEN GESTURE OVERLAY FOR CONFIRMATION
        if (isConfirmingSafe) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
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
                            
                            // Detect Y or N
                            if (isYShape(path)) {
                                onConfirmSafe()
                            } else if (isNShape(path)) {
                                onCancelSafeConfirmation()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "ARE YOU SAFE?",
                        color = HighContrastYellow,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    // No instructions shown for safety. 
                    // User knows to draw Y/N based on previous training/voice guide.
                }
            }
        }
    }
}

/**
 * Recognizes a strict UPPERCASE "P" drawn in a single continuous stroke.
 */
private fun isStrictPShape(path: List<Offset>): Boolean {
    if (path.size < 20) return false
    val start = path[0]
    var index = 0
    var maxDownY = start.y
    var foundDownstroke = false
    while (index < path.size) {
        if (path[index].y > maxDownY) maxDownY = path[index].y
        if (maxDownY - start.y > 250f) foundDownstroke = true
        if (foundDownstroke && path[index].y < maxDownY - 40f) break
        index++
    }
    if (!foundDownstroke) return false
    var backAtTop = false
    while (index < path.size) {
        if (path[index].y < start.y + 120f) {
            backAtTop = true
            break
        }
        index++
    }
    if (!backAtTop) return false
    var pushedRight = false
    var curvedDown = false
    var closedToStem = false
    val loopStartX = path[index].x
    val loopStartY = path[index].y
    while (index < path.size) {
        val p = path[index]
        if (p.x > loopStartX + 120f) pushedRight = true
        if (pushedRight && p.y > loopStartY + 100f) curvedDown = true
        if (curvedDown && p.x < loopStartX + 80f) closedToStem = true
        index++
    }
    return pushedRight && curvedDown && closedToStem
}

/**
 * Recognizes a 'Y' shape (V-shape proxy) in a single stroke.
 */
private fun isYShape(path: List<Offset>): Boolean {
    if (path.size < 15) return false
    val start = path[0]
    var index = 0
    
    // Phase 1: Down-Right
    var foundDownRight = false
    while (index < path.size) {
        val p = path[index]
        if (p.y > start.y + 150f && p.x > start.x + 80f) {
            foundDownRight = true
            break
        }
        index++
    }
    if (!foundDownRight) return false
    
    // Phase 2: Up-Right (from the bottom point)
    val bottomPoint = path[index]
    var foundUpRight = false
    while (index < path.size) {
        val p = path[index]
        if (p.y < bottomPoint.y - 100f && p.x > bottomPoint.x + 80f) {
            foundUpRight = true
            break
        }
        index++
    }
    
    return foundUpRight
}

/**
 * Recognizes an 'N' shape in a single stroke.
 */
private fun isNShape(path: List<Offset>): Boolean {
    if (path.size < 20) return false
    val start = path[0]
    var index = 0
    
    // 1. Straight Up
    var foundUp = false
    while (index < path.size) {
        if (path[index].y < start.y - 200f) {
            foundUp = true
            break
        }
        index++
    }
    if (!foundUp) return false
    
    // 2. Diagonal Down-Right
    val topPoint = path[index]
    var foundDiag = false
    while (index < path.size) {
        val p = path[index]
        if (p.y > topPoint.y + 150f && p.x > topPoint.x + 100f) {
            foundDiag = true
            break
        }
        index++
    }
    if (!foundDiag) return false
    
    // 3. Straight Up again
    val bottomPoint = path[index]
    var foundUpFinal = false
    while (index < path.size) {
        if (path[index].y < bottomPoint.y - 150f) {
            foundUpFinal = true
            break
        }
        index++
    }
    
    return foundUpFinal
}
