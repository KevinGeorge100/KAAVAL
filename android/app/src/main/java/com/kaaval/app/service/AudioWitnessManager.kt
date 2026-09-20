package com.kaaval.app.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

/**
 * KAAVAL Audio Witness Manager
 * Automatically records a short ambient audio snippet during an emergency.
 * Provides "ears on the ground" for caregivers and feeds into OpenAI Whisper/GPT-4o-mini.
 */
class AudioWitnessManager(
    private val context: Context,
    private val onRecordingFinished: (File, String) -> Unit = { _, _ -> }
) {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentFile: File? = null
    private var currentIncidentId: String = ""

    fun startRecording(incidentId: String) {
        if (isRecording) return

        try {
            currentIncidentId = incidentId
            val file = File(context.cacheDir, "witness_$incidentId.m4a")
            currentFile = file
            
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
            
            // Auto-stop after 12 seconds to optimize for Whisper transmission & response latency
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                stopRecording()
            }, 12000)

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
            
            val fileToProcess = currentFile
            val incidentId = currentIncidentId
            if (fileToProcess != null && fileToProcess.exists()) {
                onRecordingFinished(fileToProcess, incidentId) 
            }
        } catch (e: Exception) {
            Log.e("AudioWitness", "Error stopping recorder", e)
        }
    }
}
