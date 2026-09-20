package com.kaaval.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * High-Contrast Accessible Material 3 Color Scheme for KAAVAL.
 */
private val AccessibleDarkColorScheme = darkColorScheme(
    primary = HighContrastYellow,
    onPrimary = HighContrastBlack,
    secondary = EmergencyRed,
    onSecondary = PureWhite,
    background = HighContrastBlack,
    onBackground = PureWhite,
    surface = SurfaceGray,
    onSurface = PureWhite,
    error = EmergencyRed,
    onError = PureWhite
)

@Composable
fun KAAVALTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AccessibleDarkColorScheme,
        typography = Typography,
        content = content
    )
}
