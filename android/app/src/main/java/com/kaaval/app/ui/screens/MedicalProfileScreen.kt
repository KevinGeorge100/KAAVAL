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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaval.app.domain.model.MedicalProfile
import com.kaaval.app.ui.theme.EmergencyRed
import com.kaaval.app.ui.theme.HighContrastBlack
import com.kaaval.app.ui.theme.HighContrastYellow

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
            color = HighContrastYellow
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2433)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, HighContrastYellow, RoundedCornerShape(16.dp))
                .semantics {
                    contentDescription = "Medical Card for ${profile.fullName}. Blood Group: ${profile.bloodGroup}. Allergies: ${profile.allergies}. Medications: ${profile.medications}."
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
                    Badge(containerColor = EmergencyRed) {
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
                Divider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(12.dp))

                Text("ALLERGIES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighContrastYellow)
                Text(profile.allergies, fontSize = 16.sp, color = Color.White)

                Spacer(modifier = Modifier.height(12.dp))

                Text("CURRENT MEDICATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighContrastYellow)
                Text(profile.medications, fontSize = 16.sp, color = Color.White)

                Spacer(modifier = Modifier.height(12.dp))

                Text("EMERGENCY INSTRUCTIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighContrastYellow)
                Text(profile.emergencyNotes, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
