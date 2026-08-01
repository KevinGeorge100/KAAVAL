# KAAVAL Hardware Ecosystem: The Tactile Life-Line

This document outlines the hardware architecture and vision for the **KAAVAL** (Sahaaya) emergency wearable, designed specifically for visually impaired students.

## 1. Vision & Purpose
The KAAVAL Hardware is the **"Physical Shield"** of our ecosystem. While the mobile software handles coordination, the hardware ensures that help is never more than a single tactile click away.

### Core Goals:
*   **Zero-Friction Triggering**: Solve the "Pocket Problem" by providing a trigger that is always reachable (Necklace/Wrist) without finding or unlocking a phone.
*   **Tactile Reassurance**: Provide two-way haptic feedback so the user *feels* when the alert is sent and when a caregiver responds.
*   **Discreet Safety**: A jewelry-inspired design that looks like a standard locket to maintain user dignity and provide stealth in dangerous situations.

---

## 2. Technical Stack (v1.0 Prototype)

Based on the **ESP32-C3 SuperMini** platform, selected for its balance of size, cost, and RISC-V power efficiency.

| Component | Specification |
| :--- | :--- |
| **Microcontroller** | ESP32-C3 SuperMini (RISC-V Single-Core) |
| **Wireless** | Bluetooth Low Energy (BLE 5.0) via NimBLE-Arduino |
| **Battery** | 3.7V 100mAh - 250mAh Li-Po (Rechargeable) |
| **Charging** | TP4056 Module with USB-C Port |
| **Input** | High-Tactile Micro Push Button (Interrupt-driven) |
| **Feedback** | 10mm Pancake Vibration Motor + Status LED |
| **Framework** | Arduino Framework / PlatformIO |

---

## 3. Communication Protocol (GATT Architecture)

The locket acts as a BLE Peripheral. The Android app connects to the following custom services:

### Custom Emergency Service (`UUID: 0xFF01`)
*   **Trigger Characteristic (`UUID: 0xFF02`)**: 
    *   `NOTIFY`: Sends a signal to the phone when the button is pressed.
    *   *Logic*: `1` = Single Click, `2` = Double Click, `3` = Triple Click (SOS).
*   **Haptic Command (`UUID: 0xFF03`)**:
    *   `WRITE`: The Android app sends a command back to the locket.
    *   *Logic*: `1` = Short Buzz (Confirmed), `2` = Heartbeat Pattern (Help is on the way).
*   **Battery Level (`UUID: 0x2A19`)**:
    *   `READ/NOTIFY`: Standard SIG Service to monitor the locket's power.

---

## 4. Hardware Implementation Logic (For Engineers)

### Power Management (Deep Sleep)
To ensure the locket lasts for weeks/months, the ESP32-C3 must operate in **Deep Sleep** mode.
*   **Wake-up Source**: The Push Button must be connected to a GPIO that supports `esp_deep_sleep_enable_gpio_wakeup()`.
*   **Flow**: Sleep (low power) -> Button Pressed -> Wakeup -> Connect BLE -> Send SOS -> Vibrate -> Return to Sleep.

### Circuit Safety
*   **Vibration Motor**: Must be driven via a **MOSFET or NPN Transistor (e.g., 2N2222)**. Do not draw current directly from the ESP32 GPIO pins to avoid damaging the chip.

---

## 5. Prototype Roadmap

1.  **Phase 1 (Breadboard)**: Validate BLE connection with NimBLE and test "Wake from Deep Sleep" logic.
2.  **Phase 2 (Integration)**: Connect the 10mm vibration motor and validate "Heartbeat" feedback from the Android App.
3.  **Phase 3 (Miniaturization)**: Solder components into the SuperMini footprint and design a 3D-printed ergonomic casing (Locket or Pebble shape).

---

**KAAVAL Ecosystem** - *Accessibility-First Emergency Response.*
