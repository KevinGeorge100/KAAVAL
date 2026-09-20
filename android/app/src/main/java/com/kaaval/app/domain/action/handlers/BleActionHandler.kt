package com.kaaval.app.domain.action.handlers

import com.kaaval.app.domain.model.EmergencyAction
import com.kaaval.app.service.KaavalBleManager

/**
 * KAAVAL BLE Action Handler
 * Responsible for hardware communication and safely bridging with KaavalBleManager.
 */
class BleActionHandler(private val bleManager: KaavalBleManager) {
    fun handle(action: EmergencyAction) {
        // Handle BLE specific actions here
        // e.g. Triggering haptics on the wearable
    }
}
