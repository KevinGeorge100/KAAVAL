package com.kaaval.app.domain.model

data class EmergencyContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean = false
)

data class EmergencyIncident(
    val incidentId: String,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val status: String,
    val trackingUrl: String,
    val dispatchLogs: List<String> = emptyList()
)

data class MedicalProfile(
    val fullName: String,
    val age: Int,
    val bloodGroup: String,
    val allergies: String,
    val medications: String,
    val emergencyNotes: String,
    val preferredLanguage: String = "en" // "en" or "ml"
)

data class WearableDevice(
    val deviceName: String = "KAAVAL Tactile Wearable",
    val isConnected: Boolean = true,
    val batteryPercentage: Int = 92,
    val rssi: Int = -58,
    val lastSyncTime: Long = System.currentTimeMillis()
)
