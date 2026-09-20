package com.kaaval.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kaaval.app.MainActivity
import com.kaaval.app.R
import com.kaaval.app.accessibility.HapticFeedbackManager
import com.kaaval.app.accessibility.VoiceFeedbackManager
import com.kaaval.app.data.KaavalDatabase
import com.kaaval.app.data.KaavalRepository
import com.kaaval.app.data.repository.FirebaseTrackingRepository
import com.kaaval.app.domain.repository.LocationTrackingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

/**
 * KAAVAL Emergency Foreground Service
 * The active orchestrator for continuous tracking and session life-support.
 * Owns the Android runtime location loop and closed-loop caregiver reassurance listener.
 */
class EmergencyForegroundService : Service() {

    private var currentIncidentId: String? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("EmergencyService", "Coroutine error caught safely: ${throwable.message}", throwable)
    }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)
    
    private lateinit var locationManager: KaavalLocationManager
    private lateinit var repository: KaavalRepository
    private lateinit var trackingRepository: LocationTrackingRepository
    private var lastClaimedBy: String? = null
    private var lastReassuranceTimestamp: Long? = null

    companion object {
        const val CHANNEL_ID = "KAAVAL_EMERGENCY_CHANNEL"
        const val NOTIFICATION_ID = 9999
        const val EXTRA_INCIDENT_ID = "EXTRA_INCIDENT_ID"

        fun startService(context: Context, incidentId: String) {
            try {
                val intent = Intent(context, EmergencyForegroundService::class.java).apply {
                    putExtra(EXTRA_INCIDENT_ID, incidentId)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("EmergencyService", "Failed to invoke startService: ${e.message}", e)
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, EmergencyForegroundService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                android.util.Log.e("EmergencyService", "Failed to invoke stopService: ${e.message}", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = KaavalLocationManager(this)
        repository = KaavalRepository(KaavalDatabase.getDatabase(this))
        trackingRepository = FirebaseTrackingRepository()
        HapticFeedbackManager.initialize(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Critical: startForeground must execute immediately to satisfy Android 8.0+ / Android 14 requirements
        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("EmergencyService", "startForeground location fallback: ${e.message}", e)
            try {
                startForeground(NOTIFICATION_ID, buildNotification())
            } catch (inner: Exception) {
                android.util.Log.e("EmergencyService", "startForeground completely failed: ${inner.message}", inner)
            }
        }

        val incidentId = intent?.getStringExtra(EXTRA_INCIDENT_ID)
        if (incidentId != null) {
            if (incidentId != currentIncidentId) {
                currentIncidentId = incidentId
                android.util.Log.i("EmergencyService", "SERVICE_STARTED incidentId=$incidentId")
                serviceScope.launch {
                    try {
                        trackingRepository.createTrackingSession(incidentId)
                    } catch (e: Exception) {
                        android.util.Log.w("EmergencyService", "createTrackingSession error: ${e.message}")
                    }
                }
                startContinuousTracking(incidentId)
                startCaregiverReassuranceListener(incidentId)
            } else {
                android.util.Log.w("EmergencyService", "Duplicate service start for same session ignored.")
            }
        }

        return START_STICKY
    }

    private fun startCaregiverReassuranceListener(incidentId: String) {
        serviceScope.launch {
            try {
                trackingRepository.getTrackingSession(incidentId)
                    .catch { e -> android.util.Log.w("EmergencyService", "Reassurance listener error: ${e.message}") }
                    .collect { session ->
                        if (session == null) return@collect

                        // 1. Detect Caregiver Claim / Acknowledgment
                        if (!session.claimedBy.isNullOrBlank() && session.claimedBy != lastClaimedBy) {
                            lastClaimedBy = session.claimedBy
                            android.util.Log.i("EmergencyService", "Caregiver claim received: ${session.claimedBy} (ETA: ${session.claimedEta})")
                            
                            withContext(Dispatchers.Main) {
                                VoiceFeedbackManager.announceCaregiverResponse(session.claimedBy, session.claimedEta)
                            }
                            HapticFeedbackManager.vibrate(HapticFeedbackManager.HapticPattern.CAREGIVER_RESPONDING)
                        }

                        // 2. Detect Manual Reassurance Ping from Caregiver Portal
                        if (session.lastReassurancePing != null && session.lastReassurancePing != lastReassuranceTimestamp) {
                            lastReassuranceTimestamp = session.lastReassurancePing
                            android.util.Log.i("EmergencyService", "Reassurance ping received from caregiver portal")
                            
                            withContext(Dispatchers.Main) {
                                VoiceFeedbackManager.announceReassurancePing()
                            }
                            HapticFeedbackManager.vibrate(HapticFeedbackManager.HapticPattern.CAREGIVER_RESPONDING)
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("EmergencyService", "startCaregiverReassuranceListener caught: ${e.message}")
            }
        }
    }

    private fun startContinuousTracking(incidentId: String) {
        try {
            locationManager.startLocationUpdates { locationData ->
                serviceScope.launch {
                    try {
                        val session = repository.getActiveSession().first()
                        if (session != null && session.incidentId == incidentId) {
                            val updatedSession = session.copy(lastKnownLocation = locationData)
                            repository.updateSession(updatedSession)
                            trackingRepository.publishLocation(incidentId, locationData)
                            android.util.Log.d("EmergencyService", "LOCATION_UPDATE_PERSISTED accuracy=${locationData.accuracy}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("EmergencyService", "Location persistence warning: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("EmergencyService", "startLocationUpdates error: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationManager.stopLocationUpdates()
            currentIncidentId?.let { id ->
                serviceScope.launch {
                    try {
                        trackingRepository.completeTrackingSession(id)
                    } catch (e: Exception) {
                        android.util.Log.w("EmergencyService", "completeTrackingSession error: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("EmergencyService", "onDestroy error: ${e.message}")
        }
        serviceScope.cancel()
        android.util.Log.i("EmergencyService", "SERVICE_STOPPED incidentId=$currentIncidentId")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("KAAVAL EMERGENCY ACTIVE")
            .setContentText("Continuous tracking and caregiver alerting active.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency SOS Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when KAAVAL emergency live tracking is active"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
