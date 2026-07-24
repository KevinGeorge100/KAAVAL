package com.kaaval.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaval.app.domain.model.EmergencyContact
import com.kaaval.app.ui.theme.HighContrastBlack
import com.kaaval.app.ui.theme.HighContrastYellow

@Composable
fun ContactsScreen(
    contacts: List<EmergencyContact>,
    onAddContact: (name: String, phone: String, relationship: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var relationInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HighContrastBlack)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Emergency Contacts",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = HighContrastYellow
            )

            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier.semantics {
                    contentDescription = "Add new emergency contact"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = HighContrastYellow,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (contacts.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "No emergency contacts added.\nTap + to add your primary caregivers.",
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(contacts) { contact ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2433)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (contact.isPrimary) 2.dp else 0.dp,
                                color = if (contact.isPrimary) HighContrastYellow else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .semantics {
                                contentDescription = "${contact.name}, ${contact.relationship}, Phone ${contact.phoneNumber}${if (contact.isPrimary) ", Primary Emergency Call Contact" else ""}"
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = HighContrastYellow,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = contact.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${contact.relationship} • ${contact.phoneNumber}",
                                        fontSize = 14.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                            if (contact.isPrimary) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Primary Contact",
                                    tint = HighContrastYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Emergency Contact", color = HighContrastYellow) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name") }
                        )
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Phone Number") }
                        )
                        OutlinedTextField(
                            value = relationInput,
                            onValueChange = { relationInput = it },
                            label = { Text("Relationship (e.g. Mother, Sister, Caregiver)") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                                onAddContact(nameInput, phoneInput, relationInput.ifBlank { "Caregiver" })
                                nameInput = ""
                                phoneInput = ""
                                relationInput = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("SAVE CONTACT")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}
