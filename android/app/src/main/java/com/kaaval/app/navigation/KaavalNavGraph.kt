package com.kaaval.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kaaval.app.features.contacts.ContactsScreen
import com.kaaval.app.features.medical.MedicalProfileScreen
import com.kaaval.app.features.sos.MainSosScreen
import com.kaaval.app.features.wearable.WearableStatusScreen

@Composable
fun KaavalNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Sos.route,
        modifier = modifier
    ) {
        composable(ScreenRoute.Sos.route) {
            MainSosScreen()
        }
        composable(ScreenRoute.Contacts.route) {
            ContactsScreen()
        }
        composable(ScreenRoute.MedicalProfile.route) {
            MedicalProfileScreen()
        }
        composable(ScreenRoute.Wearable.route) {
            WearableStatusScreen()
        }
    }
}
