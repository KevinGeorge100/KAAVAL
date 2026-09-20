package com.kaaval.app.features.medical

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaval.app.domain.model.MedicalProfile
import com.kaaval.app.theme.HighContrastBlack
import com.kaaval.app.theme.HighContrastYellow

/**
 * Medical Profile Screen composable under com.kaaval.app.features.medical.
 */
@Composable
fun MedicalProfileScreen(
    profile: MedicalProfile? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HighContrastBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Medical Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = HighContrastYellow
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Accessible medical identity for first responders.",
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}
