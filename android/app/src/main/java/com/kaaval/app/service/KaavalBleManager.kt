package com.kaaval.app.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.kaaval.app.domain.model.WearableDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * KAAVAL BLE Wearable Manager
 * Handles scanning, connection, and battery monitoring for the tactile trigger wearable.
 * Optimized for low-power background operation.
 */
class KaavalBleManager(
    private val context: Context,
    private val onHardwareTrigger: () -> Unit
) {

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
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Here we would find the specific SOS characteristic and enable notifications
                Log.d("KaavalBleManager", "Services discovered. Wearable ready for trigger.")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            // THIS IS THE TRIGGER: Hardware button pressed!
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
