package com.kaaval.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaval.app.domain.model.MedicalProfile
import com.kaaval.app.ui.theme.EmergencyRed
import com.kaaval.app.ui.theme.HighContrastBlack
import com.kaaval.app.ui.theme.HighContrastYellow

/**
 * Emergency Medical Profile Screen
 * Hardened with Jetpack Compose Semantics for TalkBack accessibility.
 */
@Composable
fun MedicalProfileScreen(
    profile: MedicalProfile,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HighContrastBlack)
            .padding(20.dp)
    ) {
        Text(
            text = "Emergency Medical Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = HighContrastYellow,
            modifier = Modifier.semantics {
                contentDescription = "Emergency Medical Profile Header"
                stateDescription = "Medical details for first responders and caregivers"
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2433)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, HighContrastYellow, RoundedCornerShape(16.dp))
                .semantics {
                    contentDescription = "Emergency Medical Profile Card for ${profile.fullName}"
                    stateDescription = "Blood Group: ${profile.bloodGroup}. Known Allergies: ${profile.allergies}. Current Medications: ${profile.medications}. Emergency Notes: ${profile.emergencyNotes}"
                }
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profile.fullName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Badge(
                        containerColor = EmergencyRed,
                        modifier = Modifier.semantics {
                            contentDescription = "Blood Group Badge"
                            stateDescription = "Blood Group ${profile.bloodGroup}"
                        }
                    ) {
                        Text(
                            text = profile.bloodGroup,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ALLERGIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighContrastYellow,
                    modifier = Modifier.semantics { contentDescription = "Allergies Section Header" }
                )
                Text(
                    text = profile.allergies,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.semantics { contentDescription = "Known Allergies: ${profile.allergies}" }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CURRENT MEDICATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighContrastYellow,
                    modifier = Modifier.semantics { contentDescription = "Current Medications Section Header" }
                )
                Text(
                    text = profile.medications,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.semantics { contentDescription = "Current Medications: ${profile.medications}" }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "EMERGENCY INSTRUCTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighContrastYellow,
                    modifier = Modifier.semantics { contentDescription = "Emergency Instructions Section Header" }
                )
                Text(
                    text = profile.emergencyNotes,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.semantics { contentDescription = "Emergency Instructions: ${profile.emergencyNotes}" }
                )
            }
        }
    }
}
