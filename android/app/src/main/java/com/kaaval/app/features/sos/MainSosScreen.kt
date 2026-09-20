package com.kaaval.app.features.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaval.app.domain.model.EmergencyState
import com.kaaval.app.theme.EmergencyRed
import com.kaaval.app.theme.HighContrastBlack
import com.kaaval.app.theme.HighContrastYellow

/**
 * Main SOS Emergency Screen composable under com.kaaval.app.features.sos.
 * Tailored with Material 3 high-contrast accessibility standards.
 */
@Composable
fun MainSosScreen(
    emergencyState: EmergencyState = EmergencyState.Idle,
    onTriggerSos: () -> Unit = {},
    onCancelSos: () -> Unit = {},
    onResolveSos: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HighContrastBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Accessible Header Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics {
                contentDescription = "KAAVAL Emergency SOS System Header"
            }
        ) {
            Text(
                text = "KAAVAL SOS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = HighContrastYellow
            )
            Text(
                text = "Accessibility Emergency Ecosystem",
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }

        // Center Content Box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (emergencyState) {
                is EmergencyState.Idle -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(EmergencyRed)
                            .border(6.dp, HighContrastYellow, CircleShape)
                            .semantics {
                                contentDescription = "Emergency SOS Button. Tap to trigger alert."
                            }
                            .clickable { onTriggerSos() }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SOS",
                                fontSize = 40.sp,
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ACTIVATING IN ${emergencyState.secondsRemaining}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighContrastYellow
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCancelSos,
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                        ) {
                            Text("CANCEL", color = Color.White)
                        }
                    }
                }
                else -> {
                    Text(
                        text = "System Active",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }

        Text(
            text = "Voice Commands & TalkBack Enabled",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
