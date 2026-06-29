package com.clearconsent.app.recording

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.clearconsent.app.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class RecordingForegroundService : Service() {

    @Inject lateinit var notificationHelper: NotificationHelper

    private val binder = LocalBinder()
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var isPaused = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0

    inner class LocalBinder : Binder() {
        fun getService(): RecordingForegroundService = this@RecordingForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    var onDurationUpdate: ((String) -> Unit)? = null
    var onRecordingStopped: ((File, Long) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationHelper.ACTION_STOP -> stopRecording()
            NotificationHelper.ACTION_PAUSE -> togglePause()
            "ACTION_START" -> startRecording(intent)
        }
        return START_STICKY
    }

    fun startRecording(sessionId: String) {
        val intent = Intent(this, javaClass).apply {
            action = "ACTION_START"
            putExtra("session_id", sessionId)
        }
        startServiceCompat(intent)
    }

    private fun startRecording(intent: Intent?) {
        if (isRecording) return

        val sessionId = intent?.getStringExtra("session_id") ?: UUID.randomUUID().toString()
        val recordingsDir = File(filesDir, "recordings").apply { mkdirs() }
        outputFile = File(recordingsDir, "${sessionId}_${System.currentTimeMillis()}.3gp")

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            isPaused = false
            startTime = SystemClock.elapsedRealtime()
            startForeground(
                NotificationHelper.NOTIFICATION_ID_RECORDING,
                notificationHelper.buildRecordingNotification("00:00")
            )
            startDurationUpdater()
        } catch (e: IOException) {
            onError?.invoke("Failed to start recording: ${e.message}")
        }
    }

    private fun startDurationUpdater() {
        Thread {
            while (isRecording) {
                val elapsed = if (isPaused) elapsedTime
                    else elapsedTime + (SystemClock.elapsedRealtime() - startTime)
                val seconds = (elapsed / 1000) % 60
                val minutes = (elapsed / 60000) % 60
                val hours = elapsed / 3600000
                val timeStr = if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
                    else "%02d:%02d".format(minutes, seconds)
                onDurationUpdate?.invoke(timeStr)
                updateNotification(timeStr)
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun updateNotification(timeStr: String) {
        val notification = notificationHelper.buildRecordingNotification(timeStr, isPaused)
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NotificationHelper.NOTIFICATION_ID_RECORDING, notification)
    }

    fun togglePause() {
        if (!isRecording) return
        if (isPaused) {
            resumeRecording()
        } else {
            pauseRecording()
        }
    }

    private fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
        } else {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        }
        elapsedTime += SystemClock.elapsedRealtime() - startTime
        isPaused = true
        updateNotification("Paused")
    }

    private fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
        } else {
            try {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(outputFile?.absolutePath)
                    prepare()
                    start()
                }
            } catch (e: IOException) {
                onError?.invoke("Failed to resume: ${e.message}")
                return
            }
        }
        startTime = SystemClock.elapsedRealtime()
        isPaused = false
    }

    fun stopRecording() {
        if (!isRecording) return
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            val finalDuration = elapsedTime + (SystemClock.elapsedRealtime() - startTime)
            if (outputFile != null && outputFile!!.exists()) {
                onRecordingStopped?.invoke(outputFile!!, finalDuration)
            }
        } catch (e: Exception) {
            onError?.invoke("Error stopping recording: ${e.message}")
        } finally {
            isRecording = false
            isPaused = false
            notificationHelper.cancelRecordingNotification()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun startServiceCompat(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onDestroy() {
        if (isRecording) {
            try { mediaRecorder?.release() } catch (_: Exception) {}
            notificationHelper.cancelRecordingNotification()
        }
        super.onDestroy()
    }
}
