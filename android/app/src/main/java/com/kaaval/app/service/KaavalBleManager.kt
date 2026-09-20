package com.kaaval.app.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import com.kaaval.app.domain.model.WearableDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * KAAVAL BLE Wearable Manager
 * Handles scanning, connection, and battery monitoring for the tactile trigger wearable.
 * Optimized for low-power background operation.
 */
class KaavalBleManager(
    private val context: Context,
    private val onHardwareTrigger: () -> Unit
) {
    companion object {
        // These must match the UUIDs used by the ESP32 Arduino sketch.
        private val KAAVAL_SERVICE_UUID: UUID =
            UUID.fromString("f0e0d0c0-b0a0-4000-8000-000000000001")
        private val SOS_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("f0e0d0c0-b0a0-4000-8000-000000000002")
        private val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner = bluetoothAdapter?.bluetoothLeScanner

    private var bluetoothGatt: BluetoothGatt? = null
    private val _wearableState = MutableStateFlow(WearableDevice(isConnected = false))
    val wearableState: StateFlow<WearableDevice> = _wearableState.asStateFlow()

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("KaavalBleManager", "GATT Connected. Discovering services...")
                gatt.discoverServices()
                _wearableState.value = _wearableState.value.copy(isConnected = true)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("KaavalBleManager", "GATT Disconnected.")
                _wearableState.value = _wearableState.value.copy(isConnected = false)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("KaavalBleManager", "Service discovery failed with status $status")
                return
            }

            val sosCharacteristic = gatt
                .getService(KAAVAL_SERVICE_UUID)
                ?.getCharacteristic(SOS_CHARACTERISTIC_UUID)

            if (sosCharacteristic == null) {
                Log.e("KaavalBleManager", "KAAVAL SOS characteristic was not found")
                return
            }

            if (!gatt.setCharacteristicNotification(sosCharacteristic, true)) {
                Log.e("KaavalBleManager", "Could not enable SOS notifications")
                return
            }

            val descriptor = sosCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
            if (descriptor == null) {
                Log.e("KaavalBleManager", "SOS characteristic has no notification descriptor")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }
            Log.i("KaavalBleManager", "KAAVAL wearable ready for SOS notifications")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid != SOS_CHARACTERISTIC_UUID ||
                characteristic.value?.let { String(it, Charsets.UTF_8) } != "SOS") {
                return
            }

            Log.w("KaavalBleManager", "HARDWARE SOS TRIGGER RECEIVED FROM WEARABLE!")
            onHardwareTrigger()
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name?.contains("KAAVAL", ignoreCase = true) == true) {
                Log.i("KaavalBleManager", "KAAVAL Wearable Found: ${device.address}")
                stopScan()
                connectToDevice(device)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (bluetoothAdapter?.isEnabled == false) {
            Log.w("KaavalBleManager", "Bluetooth is disabled")
            return
        }

        _wearableState.value = WearableDevice(deviceName = "Searching...", isConnected = false)

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(null, settings, scanCallback)
        Log.d("KaavalBleManager", "Started BLE Scan for KAAVAL Wearable...")
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanner?.stopScan(scanCallback)
    }

    fun disconnect() {
        _wearableState.value = WearableDevice(isConnected = false)
        Log.d("KaavalBleManager", "Disconnected from wearable")
    }
}
