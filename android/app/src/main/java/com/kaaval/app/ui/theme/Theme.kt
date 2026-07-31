package com.kaaval.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * KAAVAL High-Contrast Dark Color Scheme (MVP Default)
 * Pure Black (#000000) background + Primary Yellow (#FFD600) accent.
 */
private val KaavalDarkColorScheme = darkColorScheme(
    primary = KaavalYellow,
    onPrimary = PureBlack,
    primaryContainer = HighContrastSurface,
    onPrimaryContainer = KaavalYellow,

    secondary = KaavalYellow,
    onSecondary = PureBlack,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = PureWhite,

    tertiary = ActiveGreen,
    onTertiary = PureBlack,

    background = PureBlack,
    onBackground = PureWhite,

    surface = HighContrastSurface,
    onSurface = PureWhite,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    outline = OutlineYellow,
    outlineVariant = PureWhite,

    error = EmergencyRed,
    onError = PureWhite,
    errorContainer = HighContrastSurface,
    onErrorContainer = EmergencyRed
)

/**
 * KAAVAL Application Theme Wrapper
 * Enforces Dark Theme only for MVP to optimize contrast and AMOLED battery life.
 * Dynamic color selection is disabled to ensure non-visual/low-vision consistent experience.
 */
@Composable
fun KAAVALTheme(
    darkTheme: Boolean = true, // Enforced Dark Theme only for MVP
    dynamicColor: Boolean = false, // Disabled to maintain accessible contrast guarantees
    content: @Composable () -> Unit
) {
    // Uses KaavalDarkColorScheme by default. Architecture allows easy extension for light theme in future.
    val colorScheme = KaavalDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KaavalTypography,
        shapes = KaavalShapes,
        content = content
    )
}
