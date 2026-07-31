package com.kaaval.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

/**
 * KAAVAL Battery Guardian
 * Monitors for critical battery during an active SOS.
 * Triggers a "Final SOS" message if the phone is about to die.
 */
class BatteryGuardianManager(
    private val context: Context,
    private val onCriticalBattery: (level: Int) -> Unit
) {
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                             status == BatteryManager.BATTERY_STATUS_FULL

            // If battery is below 5% and NOT charging, it's critical
            if (level in 1..5 && !isCharging) {
                Log.w("BatteryGuardian", "CRITICAL BATTERY DETECTED: $level%")
                onCriticalBattery(level)
            }
        }
    }

    fun startMonitoring() {
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d("BatteryGuardian", "Battery monitoring active.")
    }

    fun stopMonitoring() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
}
