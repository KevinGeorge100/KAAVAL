package com.kaaval.app.features.profile.domain.model

/**
 * KAAVAL Accessibility Profile
 * Defines the user's specific accessibility needs and guidance for responders.
 * This powers the Adaptive Emergency Accessibility Engine (AEAE).
 */
data class AccessibilityProfile(
    val disabilityType: DisabilityType,
    val preferredFeedback: FeedbackPreference,
    val assistanceGuidance: List<String>,
    val primaryLanguage: String = "en",
    val usesWhiteCane: Boolean = false,
    val usesWheelchair: Boolean = false,
    val emergencyContactEscalation: Boolean = true
)

enum class DisabilityType {
    VISUALLY_IMPAIRED,
    HEARING_IMPAIRED,
    ELDERLY,
    WHEELCHAIR_USER,
    COGNITIVE_DISABILITY,
    PARKINSONS,
    AUTISM,
    DEMENTIA,
    OTHER
}

enum class FeedbackPreference {
    VOICE_PRIORITY,
    HAPTIC_PRIORITY,
    VISUAL_PRIORITY,
    BALANCED
}
