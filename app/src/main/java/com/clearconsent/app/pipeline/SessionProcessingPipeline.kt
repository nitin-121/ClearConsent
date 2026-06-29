package com.clearconsent.app.pipeline

import com.clearconsent.app.domain.model.SessionStatus
import com.clearconsent.app.domain.model.Transcript
import com.clearconsent.app.domain.model.TranscriptSegment
import com.clearconsent.app.domain.repository.SessionRepository
import com.clearconsent.app.notification.NotificationHelper
import com.clearconsent.app.summarization.SummarizationEngine
import com.clearconsent.app.transcription.TranscriptionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Closed-loop pipeline: Record → Transcribe → Summarize → Deliver
 *
 * Continuously cycles through the processing chain automatically.
 * Each step triggers the next via state transitions:
 *   RECORDED → TRANSCRIBING → TRANSCRIBED → SUMMARIZING → SUMMARIZED
 *
 * Designed as a singleton so all recordings flow through one pipeline.
 */
@Singleton
class SessionProcessingPipeline @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val transcriptionEngine: TranscriptionEngine,
    private val summarizationEngine: SummarizationEngine,
    private val notificationHelper: NotificationHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Entry point — call this when a recording finishes.
     * Automatically chains: transcribe → summarize → notify
     */
    fun startProcessing(sessionId: String, audioFile: File) {
        scope.launch {
            try {
                // Step 1: Transcribe
                updateSessionStatus(sessionId, SessionStatus.TRANSCRIBING)
                val transcriptText = transcriptionEngine.transcribeFile(audioFile)

                // Split into segments for speaker-labeled display
                val segments = transcriptText.lines()
                    .filter { it.isNotBlank() }
                    .mapIndexed { i, line ->
                        TranscriptSegment(
                            id = "${sessionId}_seg_$i",
                            transcriptId = sessionId,
                            speakerLabel = null, // Speaker diarization requires cloud API
                            text = line.trim(),
                            startTime = null,
                            endTime = null
                        )
                    }

                val transcript = Transcript(
                    sessionId = sessionId,
                    rawText = transcriptText,
                    segments = segments
                )
                sessionRepository.saveTranscript(transcript)
                updateSessionStatus(sessionId, SessionStatus.TRANSCRIBED)

                // Step 2: Summarize
                updateSessionStatus(sessionId, SessionStatus.SUMMARIZING)
                val summary = summarizationEngine.summarize(sessionId, transcriptText)
                sessionRepository.saveSummary(summary)
                updateSessionStatus(sessionId, SessionStatus.SUMMARIZED)

                // Step 3: Deliver notification
                notificationHelper.showDigestNotification(1)

            } catch (e: Exception) {
                updateSessionStatus(sessionId, SessionStatus.ERROR)
            }
        }
    }

    private suspend fun updateSessionStatus(sessionId: String, status: SessionStatus) {
        sessionRepository.getSessionById(sessionId)?.let { session ->
            sessionRepository.updateSession(session.copy(status = status))
        }
    }
}
