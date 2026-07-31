package com.kaaval.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * KAAVAL High-Contrast Accessibility Color Tokens
 * Specifically designed for maximum readability and visual distinction for visually impaired users.
 * Meets WCAG AAA contrast ratio standards (Yellow on Pure Black > 19:1).
 */

// Core Brand & Safety Colors
val KaavalYellow = Color(0xFFFFD600)       // Primary yellow accent (#FFD600)
val PureBlack = Color(0xFF000000)          // Pure black background (#000000) - AMOLED optimized
val PureWhite = Color(0xFFFFFFFF)          // High contrast white text (#FFFFFF)

// Accessible Secondary & Status Colors
val EmergencyRed = Color(0xFFFF003C)       // High contrast red for critical SOS alerts
val ActiveGreen = Color(0xFF00FF66)        // High contrast green for safe / connected status

// Surface & Container Colors
val HighContrastSurface = Color(0xFF121212) // Slightly offset black for card surfaces
val SurfaceVariant = Color(0xFF1E1E1E)      // Secondary card/container background
val OnSurfaceVariant = Color(0xFFE0E0E0)    // High contrast secondary text
val OutlineYellow = Color(0xFFFFD600)       // High contrast focus & border yellow

// Backward Compatibility Aliases
val HighContrastYellow = KaavalYellow
val HighContrastBlack = PureBlack
val DarkNavyBackground = PureBlack
val SurfaceGray = HighContrastSurface
val BorderYellow = OutlineYellow
