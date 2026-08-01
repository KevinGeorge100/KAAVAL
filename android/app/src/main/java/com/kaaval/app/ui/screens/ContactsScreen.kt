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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaval.app.domain.model.EmergencyContact
import com.kaaval.app.ui.theme.HighContrastBlack
import com.kaaval.app.ui.theme.HighContrastYellow

/**
 * Emergency Contacts Screen
 * Hardened with Jetpack Compose Semantics for TalkBack accessibility.
 */
@Composable
fun ContactsScreen(
    contacts: List<EmergencyContact>,
    onAddContact: (name: String, phone: String, relationship: String) -> Unit,
    onDeleteContact: (EmergencyContact) -> Unit,
    onSetPrimary: (EmergencyContact) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var contactToDelete by remember { mutableStateOf<EmergencyContact?>(null) }
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
                color = HighContrastYellow,
                modifier = Modifier.semantics {
                    contentDescription = "Emergency Contacts Header"
                    stateDescription = "${contacts.size} contacts configured"
                }
            )

            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier.semantics {
                    role = Role.Button
                    contentDescription = "Add New Emergency Contact Button"
                    stateDescription = "Double tap to open contact setup dialog"
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
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.semantics {
                        contentDescription = "Empty Emergency Contacts List Notice"
                        stateDescription = "No emergency contacts added yet. Tap plus button at top right to add your primary caregivers."
                    }
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
                                width = if (contact.isPrimary) 2.dp else 1.dp,
                                color = if (contact.isPrimary) HighContrastYellow else Color.DarkGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (contact.isPrimary) Icons.Default.Star else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = HighContrastYellow,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
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
                                    Badge(
                                        containerColor = HighContrastYellow,
                                        modifier = Modifier.semantics {
                                            contentDescription = "Primary Call Contact Badge"
                                            stateDescription = "Selected as primary emergency auto-dial contact"
                                        }
                                    ) {
                                        Text(
                                            text = "PRIMARY",
                                            color = HighContrastBlack,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (!contact.isPrimary) {
                                    TextButton(
                                        onClick = { onSetPrimary(contact) },
                                        modifier = Modifier.semantics {
                                            role = Role.Button
                                            contentDescription = "Set ${contact.name} as Primary Contact"
                                            stateDescription = "Double tap to make this person the first contact called during emergency"
                                        }
                                    ) {
                                        Text("SET AS PRIMARY", color = HighContrastYellow, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                
                                TextButton(
                                    onClick = { contactToDelete = contact },
                                    modifier = Modifier.semantics {
                                        role = Role.Button
                                        contentDescription = "Delete ${contact.name} from Emergency Contacts"
                                        stateDescription = "Double tap to open deletion confirmation"
                                    }
                                ) {
                                    Text("REMOVE", color = Color(0xFFFF4444), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CRITICAL FIX #7: Confirmation for deleting contacts
    if (contactToDelete != null) {
        AlertDialog(
            onDismissRequest = { contactToDelete = null },
            title = { Text("Remove Contact?", fontWeight = FontWeight.Bold, color = HighContrastYellow) },
            text = { Text("Are you sure you want to remove ${contactToDelete?.name} from your emergency network?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        contactToDelete?.let { onDeleteContact(it) }
                        contactToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444))
                ) {
                    Text("YES, REMOVE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToDelete = null }) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E2433)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Add Emergency Contact",
                    color = HighContrastYellow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics {
                        contentDescription = "Add Emergency Contact Dialog Title"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Contact Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Contact Name Text Field"
                                stateDescription = if (nameInput.isEmpty()) "Empty text field" else "Value: $nameInput"
                            }
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Phone Number Text Field"
                                stateDescription = if (phoneInput.isEmpty()) "Empty text field" else "Value: $phoneInput"
                            }
                    )
                    OutlinedTextField(
                        value = relationInput,
                        onValueChange = { relationInput = it },
                        label = { Text("Relationship (e.g. Mother, Spouse)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Relationship Description Text Field"
                                stateDescription = if (relationInput.isEmpty()) "Empty text field" else "Value: $relationInput"
                            }
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighContrastYellow),
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "Save Emergency Contact Button"
                        stateDescription = "Double tap to save contact and close dialog"
                    }
                ) {
                    Text("SAVE CONTACT", color = HighContrastBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "Cancel Dialog Button"
                        stateDescription = "Double tap to dismiss dialog without saving"
                    }
                ) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E2433)
        )
    }
}
