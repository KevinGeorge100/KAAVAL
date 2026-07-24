package com.kaaval.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaval.app.domain.model.WearableDevice
import com.kaaval.app.ui.theme.ActiveGreen
import com.kaaval.app.ui.theme.HighContrastBlack
import com.kaaval.app.ui.theme.HighContrastYellow

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
            color = HighContrastYellow
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2433)),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Wearable device ${device.deviceName}. Status ${if (device.isConnected) "Connected" else "Disconnected"}. Battery ${device.batteryPercentage} percent."
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
                    Badge(containerColor = if (device.isConnected) ActiveGreen else Color.Red) {
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
                        .height(12.dp),
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
                    modifier = Modifier.fillMaxWidth(),
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
