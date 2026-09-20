package com.kaaval.app.features.contacts

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
import com.kaaval.app.domain.model.EmergencyContact
import com.kaaval.app.theme.HighContrastBlack
import com.kaaval.app.theme.HighContrastYellow

/**
 * Emergency Contacts Screen composable under com.kaaval.app.features.contacts.
 */
@Composable
fun ContactsScreen(
    contacts: List<EmergencyContact> = emptyList(),
    onAddContact: (String, String, String) -> Unit = { _, _, _ -> },
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
            text = "Emergency Contacts",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = HighContrastYellow
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Configured emergency numbers for instant SMS & call alerts.",
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}
