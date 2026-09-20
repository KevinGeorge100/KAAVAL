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
import com.kaaval.app.accessibility.VoiceFeedbackManager
import com.kaaval.app.core.accessibility.HapticFeedbackManager
import com.kaaval.app.data.KaavalDatabase
import com.kaaval.app.data.KaavalRepository
import com.kaaval.app.data.repository.FirebaseTrackingRepository
import com.kaaval.app.domain.repository.LocationTrackingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * KAAVAL Emergency Foreground Service
 * The active orchestrator for continuous tracking and session life-support.
 * Owns the Android runtime location loop and closed-loop caregiver reassurance listener.
 */
class EmergencyForegroundService : Service() {

    private var currentIncidentId: String? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private lateinit var locationManager: KaavalLocationManager
    private lateinit var repository: KaavalRepository
    private lateinit var trackingRepository: LocationTrackingRepository
    private lateinit var hapticFeedback: HapticFeedbackManager
    private var lastClaimedBy: String? = null
    private var lastReassuranceTimestamp: Long? = null

    companion object {
        const val CHANNEL_ID = "KAAVAL_EMERGENCY_CHANNEL"
        const val NOTIFICATION_ID = 9999
        const val EXTRA_INCIDENT_ID = "EXTRA_INCIDENT_ID"

        fun startService(context: Context, incidentId: String) {
            val intent = Intent(context, EmergencyForegroundService::class.java).apply {
                putExtra(EXTRA_INCIDENT_ID, incidentId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, EmergencyForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = KaavalLocationManager(this)
        repository = KaavalRepository(KaavalDatabase.getDatabase(this))
        trackingRepository = FirebaseTrackingRepository()
        hapticFeedback = HapticFeedbackManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val incidentId = intent?.getStringExtra(EXTRA_INCIDENT_ID)
        
        if (incidentId != null) {
            if (incidentId != currentIncidentId) {
                currentIncidentId = incidentId
                android.util.Log.i("EmergencyService", "SERVICE_STARTED incidentId=$incidentId")
                serviceScope.launch {
                    trackingRepository.createTrackingSession(incidentId)
                }
                startContinuousTracking(incidentId)
                startCaregiverReassuranceListener(incidentId)
            } else {
                android.util.Log.w("EmergencyService", "Duplicate service start for same session ignored.")
            }
        }

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    private fun startCaregiverReassuranceListener(incidentId: String) {
        serviceScope.launch {
            trackingRepository.getTrackingSession(incidentId).collect { session ->
                if (session == null) return@collect

                // 1. Detect Caregiver Claim / Acknowledgment
                if (!session.claimedBy.isNullOrBlank() && session.claimedBy != lastClaimedBy) {
                    lastClaimedBy = session.claimedBy
                    android.util.Log.i("EmergencyService", "Caregiver claim received: ${session.claimedBy} (ETA: ${session.claimedEta})")
                    
                    withContext(Dispatchers.Main) {
                        VoiceFeedbackManager.announceCaregiverResponse(session.claimedBy, session.claimedEta)
                    }
                    hapticFeedback.triggerReassuranceHeartbeat()
                }

                // 2. Detect Manual Reassurance Ping from Caregiver Portal
                if (session.lastReassurancePing != null && session.lastReassurancePing != lastReassuranceTimestamp) {
                    lastReassuranceTimestamp = session.lastReassurancePing
                    android.util.Log.i("EmergencyService", "Reassurance ping received from caregiver portal")
                    
                    withContext(Dispatchers.Main) {
                        VoiceFeedbackManager.announceReassurancePing()
                    }
                    hapticFeedback.triggerReassuranceHeartbeat()
                }
            }
        }
    }

    private fun startContinuousTracking(incidentId: String) {
        // Start the Fused Location update loop
        locationManager.startLocationUpdates { locationData ->
            serviceScope.launch {
                val session = repository.getActiveSession().first()
                if (session != null && session.incidentId == incidentId) {
                    val updatedSession = session.copy(lastKnownLocation = locationData)
                    repository.updateSession(updatedSession)
                    trackingRepository.publishLocation(incidentId, locationData)
                    android.util.Log.d("EmergencyService", "LOCATION_UPDATE_PERSISTED accuracy=${locationData.accuracy}")
                } else {
                    android.util.Log.e("EmergencyService", "Update failed: No matching active session found.")
                    stopSelf()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopLocationUpdates()
        currentIncidentId?.let {
            serviceScope.launch {
                trackingRepository.completeTrackingSession(it)
            }
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
            manager.createNotificationChannel(channel)
        }
    }
}
