package com.kaaval.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kaaval.app.domain.model.MedicalProfile

@Entity(tableName = "medical_profile")
data class MedicalProfileEntity(
    @PrimaryKey val id: Int = 1, // Single user profile
    val fullName: String,
    val age: Int,
    val bloodGroup: String,
    val allergies: String,
    val medications: String,
    val emergencyNotes: String,
    val preferredLanguage: String = "en"
) {
    fun toDomainModel(): MedicalProfile {
        return MedicalProfile(
            fullName = fullName,
            age = age,
            bloodGroup = bloodGroup,
            allergies = allergies,
            medications = medications,
            emergencyNotes = emergencyNotes,
            preferredLanguage = preferredLanguage
        )
    }

    companion object {
        fun fromDomainModel(profile: MedicalProfile): MedicalProfileEntity {
            return MedicalProfileEntity(
                id = 1,
                fullName = profile.fullName,
                age = profile.age,
                bloodGroup = profile.bloodGroup,
                allergies = profile.allergies,
                medications = profile.medications,
                emergencyNotes = profile.emergencyNotes,
                preferredLanguage = profile.preferredLanguage
            )
        }
    }
}
