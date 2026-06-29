package com.clearconsent.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.clearconsent.app.domain.model.*
import com.clearconsent.app.domain.repository.SessionRepository
import com.clearconsent.app.notification.NotificationHelper
import com.clearconsent.app.summarization.SummarizationEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class DailyDigestWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sessionRepository: SessionRepository,
    private val summarizationEngine: SummarizationEngine,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = todayStart + 86400000L

        var sessionCount = 0
        sessionRepository.getSessionsByDate(todayStart, todayEnd).collect { sessions ->
            sessionCount = sessions.size
            for (session in sessions) {
                // Process sessions that weren't auto-processed by the pipeline
                if (session.status == SessionStatus.TRANSCRIBED) {
                    val transcript = sessionRepository.getTranscript(session.id)
                    if (transcript != null) {
                        val summary = summarizationEngine.summarize(session.id, transcript.rawText)
                        sessionRepository.saveSummary(summary)
                        sessionRepository.updateSession(session.copy(status = SessionStatus.SUMMARIZED))
                    }
                }
            }
        }

        if (sessionCount > 0) {
            notificationHelper.showDigestNotification(sessionCount)
        }

        return Result.success()
    }

    companion object {
        fun scheduleDaily(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyDigestWorker>(
                24, java.util.concurrent.TimeUnit.HOURS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "daily_digest",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}
