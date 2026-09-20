package com.kaaval.app.navigation

/**
 * Screen routes definition for Jetpack Compose Navigation.
 */
sealed class ScreenRoute(val route: String, val title: String) {
    data object Sos : ScreenRoute("sos", "SOS Emergency")
    data object Contacts : ScreenRoute("contacts", "Emergency Contacts")
    data object MedicalProfile : ScreenRoute("medical_profile", "Medical Profile")
    data object Wearable : ScreenRoute("wearable", "Wearable Status")
}
