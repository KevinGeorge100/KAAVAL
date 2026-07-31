package com.kaaval.app.accessibility

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * KAAVAL Haptic Feedback Engine (Singleton)
 * Lifecycle-safe, thread-safe tactile vibration engine for visually impaired users.
 * Compatible with Android API 26+ (VibrationEffect) and Android 12+ API 31+ (VibratorManager).
 */
object HapticFeedbackManager {

    private const val TAG = "HapticFeedbackManager"

    private var vibrator: Vibrator? = null
    private var isInitialized = false

    enum class HapticPattern {
        SOS_HOLD,
        COUNTDOWN_TICK,
        COUNTDOWN_CANCELLED,
        SOS_ACTIVATED,
        SMS_SENT,
        CALL_STARTED,
        LOCATION_ACQUIRED,
        LIVE_TRACKING_STARTED,
        SUCCESS,
        ERROR,
        LOW_BATTERY,
        NO_INTERNET,
        GPS_DISABLED,
        CAREGIVER_RESPONDING // New pattern for response assurance
    }

    /**
     * Initializes the Vibrator service automatically on app startup or context binding.
     */
    @Synchronized
    fun initialize(context: Context) {
        if (isInitialized && vibrator != null) return

        try {
            val appContext = context.applicationContext
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            isInitialized = vibrator?.hasVibrator() == true
            Log.d(TAG, "HapticFeedbackManager initialized. Hardware vibrator available: $isInitialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing HapticFeedbackManager: ${e.message}", e)
            vibrator = null
            isInitialized = false
        }
    }

    /**
     * Triggers a specific tactile vibration pattern. Safe against missing hardware or permissions.
     */
    fun vibrate(pattern: HapticPattern) {
        val currentVibrator = vibrator
        if (!isInitialized || currentVibrator == null || !currentVibrator.hasVibrator()) {
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = createVibrationEffect(pattern)
                if (effect != null) {
                    currentVibrator.vibrate(effect)
                }
            } else {
                val (timings, _) = getPatternArrays(pattern)
                if (timings.isNotEmpty()) {
                    @Suppress("DEPRECATION")
                    currentVibrator.vibrate(timings, -1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute vibration pattern $pattern: ${e.message}")
        }
    }

    /**
     * Cancels any active vibration immediately.
     */
    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling vibration: ${e.message}")
        }
    }

    /**
     * Shuts down HapticFeedbackManager cleanly.
     */
    fun shutdown() {
        cancel()
        vibrator = null
        isInitialized = false
        Log.d(TAG, "HapticFeedbackManager shut down")
    }

    // Backward-compatibility helpers
    fun triggerCountdownPulse() = vibrate(HapticPattern.COUNTDOWN_TICK)
    fun triggerSosActivePattern() = vibrate(HapticPattern.SOS_ACTIVATED)
    fun triggerCancellationRumble() = vibrate(HapticPattern.COUNTDOWN_CANCELLED)

    private fun createVibrationEffect(pattern: HapticPattern): VibrationEffect? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null

        return when (pattern) {
            HapticPattern.SOS_HOLD -> {
                // Short distinct tactile pulse (50ms)
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            HapticPattern.COUNTDOWN_TICK -> {
                // Very short pulse every second (35ms)
                VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            HapticPattern.COUNTDOWN_CANCELLED -> {
                // Descending double rumble pulse
                val timings = longArrayOf(0, 100, 60, 150)
                val amplitudes = intArrayOf(0, 255, 0, 100)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.SOS_ACTIVATED -> {
                // Long strong vibration sequence (... --- ...)
                val timings = longArrayOf(0, 100, 100, 100, 100, 100, 200, 300, 200, 300, 200, 300, 200, 100, 100, 100, 100, 100)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.SMS_SENT -> {
                // Double confirmation pulse
                val timings = longArrayOf(0, 80, 60, 80)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.CALL_STARTED -> {
                // Medium steady pulse (300ms)
                VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            HapticPattern.LOCATION_ACQUIRED -> {
                // Triple short pulse
                val timings = longArrayOf(0, 60, 40, 60, 40, 60)
                val amplitudes = intArrayOf(0, 180, 0, 220, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.LIVE_TRACKING_STARTED -> {
                // Long-short-long pattern
                val timings = longArrayOf(0, 350, 100, 100, 100, 350)
                val amplitudes = intArrayOf(0, 255, 0, 150, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.SUCCESS -> {
                // Two pleasant pulses
                val timings = longArrayOf(0, 100, 80, 150)
                val amplitudes = intArrayOf(0, 150, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.ERROR -> {
                // Rapid repeated error pulses
                val timings = longArrayOf(0, 50, 50, 50, 50, 50, 50, 50)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.LOW_BATTERY -> {
                // Slow double pulse
                val timings = longArrayOf(0, 200, 250, 200)
                val amplitudes = intArrayOf(0, 120, 0, 120)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.NO_INTERNET -> {
                // Long pause then warning pulse
                val timings = longArrayOf(0, 400, 100, 150)
                val amplitudes = intArrayOf(0, 100, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.GPS_DISABLED -> {
                // Two long pulses
                val timings = longArrayOf(0, 450, 200, 450)
                val amplitudes = intArrayOf(0, 220, 0, 220)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.CAREGIVER_RESPONDING -> {
                // Reassuring "Heartbeat" pulse (Double tap)
                val timings = longArrayOf(0, 100, 100, 100, 800) // Pulse-pulse-pause
                val amplitudes = intArrayOf(0, 150, 0, 150, 0)
                VibrationEffect.createWaveform(timings, amplitudes, 0) // Repeat indefinitely to provide constant assurance
            }
        }
    }

    private fun getPatternArrays(pattern: HapticPattern): Pair<LongArray, IntArray> {
        return when (pattern) {
            HapticPattern.SOS_HOLD -> Pair(longArrayOf(0, 50), intArrayOf(0, 255))
            HapticPattern.COUNTDOWN_TICK -> Pair(longArrayOf(0, 35), intArrayOf(0, 255))
            HapticPattern.COUNTDOWN_CANCELLED -> Pair(longArrayOf(0, 100, 60, 150), intArrayOf(0, 255, 0, 100))
            HapticPattern.SOS_ACTIVATED -> Pair(
                longArrayOf(0, 100, 100, 100, 100, 100, 200, 300, 200, 300, 200, 300, 200, 100, 100, 100, 100, 100),
                intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255)
            )
            HapticPattern.SMS_SENT -> Pair(longArrayOf(0, 80, 60, 80), intArrayOf(0, 200, 0, 255))
            HapticPattern.CALL_STARTED -> Pair(longArrayOf(0, 300), intArrayOf(0, 255))
            HapticPattern.LOCATION_ACQUIRED -> Pair(longArrayOf(0, 60, 40, 60, 40, 60), intArrayOf(0, 180, 0, 220, 0, 255))
            HapticPattern.LIVE_TRACKING_STARTED -> Pair(longArrayOf(0, 350, 100, 100, 100, 350), intArrayOf(0, 255, 0, 150, 0, 255))
            HapticPattern.SUCCESS -> Pair(longArrayOf(0, 100, 80, 150), intArrayOf(0, 150, 0, 255))
            HapticPattern.ERROR -> Pair(longArrayOf(0, 50, 50, 50, 50, 50, 50, 50), intArrayOf(0, 255, 0, 255, 0, 255, 0, 255))
            HapticPattern.LOW_BATTERY -> Pair(longArrayOf(0, 200, 250, 200), intArrayOf(0, 120, 0, 120))
            HapticPattern.NO_INTERNET -> Pair(longArrayOf(0, 400, 100, 150), intArrayOf(0, 100, 0, 255))
            HapticPattern.GPS_DISABLED -> Pair(longArrayOf(0, 450, 200, 450), intArrayOf(0, 220, 0, 220))
            HapticPattern.CAREGIVER_RESPONDING -> Pair(longArrayOf(0, 100, 100, 100, 800), intArrayOf(0, 150, 0, 150, 0))
        }
    }
}
