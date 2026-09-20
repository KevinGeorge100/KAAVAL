package com.kaaval.app.features.profile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kaaval.app.features.profile.domain.model.AccessibilityProfile
import com.kaaval.app.features.profile.domain.model.DisabilityType
import com.kaaval.app.features.profile.domain.model.FeedbackPreference

@Entity(tableName = "accessibility_profile")
data class AccessibilityProfileEntity(
    @PrimaryKey val id: Int = 1,
    val disabilityType: String,
    val preferredFeedback: String,
    val assistanceGuidance: String, // Comma-separated list
    val primaryLanguage: String,
    val usesWhiteCane: Boolean,
    val usesWheelchair: Boolean,
    val emergencyContactEscalation: Boolean
) {
    fun toDomainModel(): AccessibilityProfile {
        return AccessibilityProfile(
            disabilityType = DisabilityType.valueOf(disabilityType),
            preferredFeedback = FeedbackPreference.valueOf(preferredFeedback),
            assistanceGuidance = assistanceGuidance.split(",").filter { it.isNotBlank() },
            primaryLanguage = primaryLanguage,
            usesWhiteCane = usesWhiteCane,
            usesWheelchair = usesWheelchair,
            emergencyContactEscalation = emergencyContactEscalation
        )
    }

    companion object {
        fun fromDomainModel(profile: AccessibilityProfile): AccessibilityProfileEntity {
            return AccessibilityProfileEntity(
                id = 1,
                disabilityType = profile.disabilityType.name,
                preferredFeedback = profile.preferredFeedback.name,
                assistanceGuidance = profile.assistanceGuidance.joinToString(","),
                primaryLanguage = profile.primaryLanguage,
                usesWhiteCane = profile.usesWhiteCane,
                usesWheelchair = profile.usesWheelchair,
                emergencyContactEscalation = profile.emergencyContactEscalation
            )
        }
    }
}
