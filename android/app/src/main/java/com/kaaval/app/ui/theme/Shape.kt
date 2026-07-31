package com.kaaval.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * KAAVAL Accessible Shape & Layout Specifications
 * Provides distinct rounded corner shapes for clear visual boundaries and UI hierarchy.
 */

// Minimum accessibility touch target standard (Google Accessibility Guidelines)
val MinTouchTargetSize = 48.dp

val KaavalShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
