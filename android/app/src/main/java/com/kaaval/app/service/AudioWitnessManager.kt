package com.kaaval.app.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

/**
 * KAAVAL Audio Witness Manager
 * Automatically records a short audio snippet during an emergency.
 * Provides "ears on the ground" for caregivers.
 */
class AudioWitnessManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    fun startRecording(incidentId: String) {
        if (isRecording) return

        try {
            val file = File(context.cacheDir, "witness_$incidentId.m4a")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            
            isRecording = true
            Log.i("AudioWitness", "Emergency audio recording started: ${file.name}")
            
            // Auto-stop after 15 seconds to save battery/data
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                stopRecording()
            }, 15000)

        } catch (e: Exception) {
            Log.e("AudioWitness", "Failed to start recording", e)
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            Log.i("AudioWitness", "Emergency audio recording stopped and saved.")
        } catch (e: Exception) {
            Log.e("AudioWitness", "Error stopping recorder", e)
        }
    }
}
