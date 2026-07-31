package com.kaaval.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaval.app.domain.model.WearableDevice
import com.kaaval.app.ui.theme.ActiveGreen
import com.kaaval.app.ui.theme.HighContrastBlack
import com.kaaval.app.ui.theme.HighContrastYellow

/**
 * BLE Wearable Hardware Status Screen
 * Hardened with Jetpack Compose Semantics for TalkBack accessibility.
 */
@Composable
fun WearableStatusScreen(
    device: WearableDevice,
    onTestTactileVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HighContrastBlack)
            .padding(20.dp)
    ) {
        Text(
            text = "BLE Wearable Status",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = HighContrastYellow,
            modifier = Modifier.semantics {
                contentDescription = "BLE Wearable Status Header"
                stateDescription = "Bluetooth Low Energy hardware tactile trigger monitor"
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2433)),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Wearable Hardware Status Card for ${device.deviceName}"
                    stateDescription = "Connection Status: ${if (device.isConnected) "Connected" else "Disconnected"}. Battery level: ${device.batteryPercentage} percent."
                }
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = device.deviceName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Badge(
                        containerColor = if (device.isConnected) ActiveGreen else Color.Red,
                        modifier = Modifier.semantics {
                            contentDescription = "BLE Connection Badge"
                            stateDescription = if (device.isConnected) "Device Connected" else "Device Disconnected"
                        }
                    ) {
                        Text(
                            text = if (device.isConnected) "CONNECTED" else "DISCONNECTED",
                            color = HighContrastBlack,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { device.batteryPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .semantics {
                            contentDescription = "Wearable Battery Level Indicator Bar"
                            stateDescription = "${device.batteryPercentage} percent remaining"
                        },
                    color = HighContrastYellow,
                    trackColor = Color.DarkGray,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Battery Level: ${device.batteryPercentage}%",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onTestTactileVibration,
                    colors = ButtonDefaults.buttonColors(containerColor = HighContrastYellow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            role = Role.Button
                            contentDescription = "Test Tactile Vibration Feedback Button"
                            stateDescription = "Double tap to trigger test vibration pulse pattern on phone and wearable"
                        },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "TEST TACTILE VIBRATION",
                        color = HighContrastBlack,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
