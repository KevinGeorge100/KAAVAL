# ⌚ KAAVAL BLE Wearable Hardware Specification

---
**Document Version:** 1.0  
**Target Device:** KAAVAL Tactile Wearable Wristband / Pendant  
**Primary User:** Visually Impaired Individuals  
**EEE Hardware Leads:** Navami, Adwaid, Jewel *(Electrical & Electronics Engineering)*  
**Software Lead:** Kevin George *(Android BLE Manager & State Engine)*  
---

## 1. Overview & Objectives
The KAAVAL Wearable is a compact, low-power Bluetooth Low Energy (BLE) device that allows a visually impaired person to trigger an emergency SOS immediately without searching for or unlocking their smartphone.

---

## 2. Microcontroller & Wireless Module Selection
- **Primary MCU:** **ESP32-C3-Mini** or **nRF52840 (Seeed Studio XIAO BLE)**
  - *Why:* Built-in BLE 5.0, low power sleep mode (< 5µA), integrated antenna, small footprint (21mm x 17.5mm).

---

## 3. Hardware Component List
1. **Microcontroller**: ESP32-C3 / nRF52840 BLE module.
2. **Tactile Switch**: 12mm x 12mm High-Travel Raised Push Button (easy to feel by touch).
3. **Haptic Motor**: ERM (Eccentric Rotating Mass) 3V Vibration Disc Motor with NPN transistor driver (2N2222 / Transistor gate) + flyback diode.
4. **Power & Charging**:
   - 3.7V 300mAh LiPo Rechargeable Battery.
   - TP4056 or MCP73831 LiPo Charger IC with Type-C USB port.
   - 3.3V Low-Dropout Voltage Regulator (LDO) e.g., AP2112K-3.3.
5. **Status LED**: Dual-color LED (Red/Green) or RGB LED for battery charging and BLE pairing indication.

---

## 4. Circuit Diagram Blueprint
```text
[ 3.7V LiPo Battery ] ───► [ TP4056 Charge IC ] ───► [ AP2112 3.3V LDO ] ───► [ ESP32-C3 MCU ]
                                  ▲                                               │
                                  │ (USB Type-C)                                  ├─► GPIO 9 : Tactile Button (Pull-Up)
                                                                                  ├─► GPIO 4 : Haptic Motor Driver
                                                                                  └─► GPIO 3 : Battery Voltage Sensing (ADC)
```

---

## 5. BLE GATT Service Definition
- **Service UUID:** `0000KAAV-0000-1000-8000-00805F9B34FB`
- **SOS Alert Characteristic (Notify):** `0000ALERT-0000-1000-8000-00805F9B34FB`
  - Value `0x01` = Single Press / Countdown
  - Value `0x02` = SOS Triggered
  - Value `0x00` = Idle / Cancelled
- **Battery Level Characteristic (Read/Notify):** Standard `0x2A19` (0-100%)

---

## 6. EEE Work Breakdown Structure
- **Navami**: Schematic capture in EasyEDA / KiCAD and tactile switch debouncing logic.
- **Adwaid**: Power management circuit, battery voltage divider calculation, and USB Type-C charging.
- **Jewel**: 3D enclosure CAD modeling (wristband / pendant style) with tactile button guidance ring.
- **Kevin George**: Android BLE Scanner, GATT client, and emergency service listener.
