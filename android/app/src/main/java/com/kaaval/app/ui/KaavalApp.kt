package com.kaaval.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kaaval.app.navigation.KaavalNavGraph
import com.kaaval.app.navigation.ScreenRoute
import com.kaaval.app.theme.HighContrastBlack
import com.kaaval.app.theme.HighContrastYellow
import com.kaaval.app.theme.KAAVALTheme

/**
 * Root Compose Entry Point for the KAAVAL application.
 * Provides accessible navigation structure and Material 3 high contrast scaffolding.
 */
@Composable
fun KaavalApp() {
    KAAVALTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val navigationItems = listOf(
            Triple(ScreenRoute.Sos, Icons.Default.Home, "SOS Emergency Screen"),
            Triple(ScreenRoute.Contacts, Icons.Default.People, "Emergency Contacts Screen"),
            Triple(ScreenRoute.MedicalProfile, Icons.Default.AccountBox, "Medical Profile Screen"),
            Triple(ScreenRoute.Wearable, Icons.Default.Settings, "Wearable Device Screen")
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = HighContrastBlack,
                        modifier = Modifier.semantics {
                            contentDescription = "Main Navigation Bar"
                        }
                    ) {
                        navigationItems.forEach { (screen, icon, description) ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = description
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 12.sp,
                                        color = HighContrastYellow
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = HighContrastBlack,
                                    indicatorColor = HighContrastYellow
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                KaavalNavGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
