package com.clearconsent.app.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.clearconsent.app.MainActivity
import com.clearconsent.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_RECORDING = "recording_service"
        const val CHANNEL_DIGEST = "daily_digest"
        const val NOTIFICATION_ID_RECORDING = 1001
        const val NOTIFICATION_ID_DIGEST = 1002
        const val ACTION_PAUSE = "com.clearconsent.app.ACTION_PAUSE"
        const val ACTION_STOP = "com.clearconsent.app.ACTION_STOP"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        val recordingChannel = NotificationChannel(
            CHANNEL_RECORDING,
            context.getString(R.string.recording_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.recording_channel_desc)
            setShowBadge(false)
        }

        val digestChannel = NotificationChannel(
            CHANNEL_DIGEST,
            "Daily Digest",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily meeting summary notifications"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(recordingChannel)
        manager.createNotificationChannel(digestChannel)
    }

    fun buildRecordingNotification(
        durationText: String,
        isPaused: Boolean = false
    ): Notification {
        val stopIntent = PendingIntent.getService(
            context, 0,
            Intent(context, com.clearconsent.app.recording.RecordingForegroundService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = PendingIntent.getService(
            context, 1,
            Intent(context, com.clearconsent.app.recording.RecordingForegroundService::class.java).apply {
                action = ACTION_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = PendingIntent.getActivity(
            context, 2,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("open_recording", true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_RECORDING)
            .setContentTitle(if (isPaused) "Recording Paused" else "Recording")
            .setContentText("Duration: $durationText")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(!isPaused)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_media_pause, if (isPaused) "Resume" else "Pause", pauseIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .build()
    }

    fun showDigestNotification(sessionCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_digest", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DIGEST)
            .setContentTitle("Daily Digest Ready")
            .setContentText("$sessionCount session(s) summarized today")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_DIGEST, notification)
    }

    fun cancelRecordingNotification() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID_RECORDING)
    }
}
