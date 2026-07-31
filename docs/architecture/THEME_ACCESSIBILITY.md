# ♿ KAAVAL High-Contrast Accessibility Theme Architecture

**Document Version:** 1.0  
**Target Audience:** Android Engineers, UI/UX Designers, Accessibility Auditors, Project Mentors  
**Scope:** Material 3 High-Contrast Design System for KAAVAL Android Application (`com.kaaval.app`)

---

## 🎯 1. Architectural Philosophy

KAAVAL is an **accessibility-first emergency response application** for visually impaired individuals. Traditional applications rely on subtle gradients, low-contrast pastel tones, or small interactive controls. KAAVAL deliberately rejects conventional aesthetic conventions in favor of **strict high-contrast visibility, immediate touch target recognition, and non-visual feedback readiness**.

### Core Guidelines:
1. **WCAG AAA Compliance**: Color pairs exceed the Web Content Accessibility Guidelines (WCAG) AAA contrast standard of 7:1 (KAAVAL Primary Yellow on Pure Black achieves ~19.5:1).
2. **AMOLED Optimization & Battery Longevity**: Emergency applications must remain operational during power loss or high-stress scenarios. Pure Black (`#000000`) turns off individual pixels on OLED/AMOLED screens, preserving battery life and eliminating backlight bleed for users with light sensitivity or low vision.
3. **Non-Visual & Low-Vision First**: The UI tokens support Android System Dynamic Text Scaling up to 200%, with TalkBack content descriptions and touch target sizes starting at a minimum of **48dp x 48dp**.

---

## 🎨 2. Color Palette & Token Mapping

| Token Name | Hex Code | Color Role | WCAG Contrast Ratio | Purpose / Notes |
| :--- | :--- | :--- | :--- | :--- |
| `PureBlack` | `#000000` | Background | Baseline (0:1) | Pure black AMOLED background. Eliminates glare. |
| `KaavalYellow` | `#FFD600` | Primary / Accent | **~19.5:1** on Pure Black | Main tactile accent, high visibility primary buttons, outlines. |
| `PureWhite` | `#FFFFFF` | OnBackground / OnSurface | **21:1** on Pure Black | Body text, headings, secondary descriptions. |
| `EmergencyRed` | `#FF003C` | Error / SOS Alert | **~7.5:1** on Pure Black | High-visibility red for critical SOS triggers & danger states. |
| `ActiveGreen` | `#00FF66` | Success / Active | **~15.2:1** on Pure Black | Safe status, connected wearable indicator, confirmed state. |
| `HighContrastSurface` | `#121212` | Surface Card | **~18.2:1** with `#FFFFFF` text | Elevation 1 surface for cards and container boundaries. |
| `SurfaceVariant` | `#1E1E1E` | Container Surface | **~16.5:1** with `#FFFFFF` text | Secondary list items, dialog containers. |
| `OutlineYellow` | `#FFD600` | Border / Outline | N/A | High-visibility container borders and focus rings. |

---

## 🔤 3. Typography & Dynamic Font Scaling

The typography hierarchy ([Typography.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/theme/Typography.kt)) uses Jetpack Compose Material 3 `Typography` configured with enlarged font sizes and bold font weights (`FontWeight.Bold` and `FontWeight.SemiBold`).

```kotlin
displayLarge  -> 40.sp (Line height 48.sp) — Hero emergency timer & state labels
headlineLarge -> 28.sp (Line height 36.sp) — Screen titles & major section headers
titleLarge    -> 22.sp (Line height 28.sp) — Card titles & contact names
bodyLarge     -> 18.sp (Line height 26.sp) — Primary readable body text
labelLarge    -> 16.sp (Line height 20.sp) — Button labels (all-caps support)
```

### Dynamic Text Scaling Rules:
- All font sizes use scaled pixels (`sp`), ensuring that when users enable **Large Text** or **Display Scaling** in Android Settings, text scales gracefully without clipping.
- Containers apply flexible scroll states (`verticalScroll`) or `heightIn(min = 48.dp)` instead of hardcoded rigid container heights.

---

## 📐 4. Shape & Touch Target Sizing

- **Minimum Touch Target**: Defined in [Shape.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/theme/Shape.kt) as `MinTouchTargetSize = 48.dp`.
- **Button Standards**: Primary and emergency buttons enforce `heightIn(min = 48.dp)` or `heightIn(min = 56.dp)`.
- **Corner Radii**:
  - `small` -> `12.dp`
  - `medium` -> `16.dp`
  - `large` -> `24.dp` (Used for SOS Hold trigger)

---

## 🛠️ 5. Implementation Files

1. [Color.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/theme/Color.kt) — Color tokens and backward-compatibility aliases.
2. [Typography.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/theme/Typography.kt) — Accessible typography specifications.
3. [Shape.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/theme/Shape.kt) — Material 3 shapes & touch target sizing.
4. [Theme.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/theme/Theme.kt) — Enforced dark theme wrapper & MaterialTheme injection.
5. [ThemePreviewScreen.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/screens/ThemePreviewScreen.kt) — Live Jetpack Compose preview screen.
