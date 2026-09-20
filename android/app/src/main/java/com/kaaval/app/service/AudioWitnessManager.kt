package com.kaaval.app.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File

/** Short, best-effort witness capture. Failure must never interrupt SOS. */
class AudioWitnessManager(
    private val context: Context,
    private val onRecordingFinished: (File, String) -> Unit = { _, _ -> }
) {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentFile: File? = null
    private var currentIncidentId = ""
    private val handler = Handler(Looper.getMainLooper())
    private val stopTask = Runnable { stopRecording() }

    @Synchronized
    fun startRecording(incidentId: String) {
        if (isRecording) return
        try {
            currentIncidentId = incidentId
            currentFile = File.createTempFile("witness_", ".m4a", context.cacheDir)
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            // Retain before prepare/start so partial initialization can be released.
            mediaRecorder = recorder
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(currentFile!!.absolutePath)
            recorder.prepare()
            recorder.start()
            isRecording = true
            handler.postDelayed(stopTask, 12000)
        } catch (e: Exception) {
            Log.e("AudioWitness", "Failed to start recording", e)
            releaseRecorder()
            currentFile?.delete()
            currentFile = null
        }
    }

    @Synchronized
    fun stopRecording() {
        handler.removeCallbacks(stopTask)
        if (!isRecording) return
        val file = currentFile
        var saved = false
        try {
            mediaRecorder?.stop()
            saved = true
        } catch (e: Exception) {
            Log.w("AudioWitness", "Recording could not be finalized", e)
        } finally {
            releaseRecorder()
            currentFile = null
        }
        try {
            if (saved && file != null && file.exists() && file.length() > 0) {
                onRecordingFinished(file, currentIncidentId)
            } else {
                file?.delete()
            }
        } catch (e: Exception) {
            Log.e("AudioWitness", "Recording callback failed", e)
        }
    }

    private fun releaseRecorder() {
        handler.removeCallbacks(stopTask)
        try { mediaRecorder?.release() } catch (e: Exception) {
            Log.w("AudioWitness", "Recorder release failed", e)
        } finally {
            mediaRecorder = null
            isRecording = false
        }
    }
}
