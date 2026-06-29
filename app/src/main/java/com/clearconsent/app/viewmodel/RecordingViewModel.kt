package com.clearconsent.app.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.clearconsent.app.domain.model.Session
import com.clearconsent.app.domain.model.SessionStatus
import com.clearconsent.app.domain.repository.SessionRepository
import com.clearconsent.app.pipeline.SessionProcessingPipeline
import com.clearconsent.app.recording.RecordingForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Manages the recording lifecycle and triggers the closed-loop pipeline
 * when recording completes.
 *
 * Pipeline flow: RECORDING → RECORDED → [pipeline: TRANSCRIBING → TRANSCRIBED → SUMMARIZING → SUMMARIZED]
 */
@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val app: Application,
    private val sessionRepository: SessionRepository,
    private val processingPipeline: SessionProcessingPipeline
) : AndroidViewModel(app) {

    private var recordingService: RecordingForegroundService? = null
    private var bound = false

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _durationText = MutableStateFlow("00:00")
    val durationText: StateFlow<String> = _durationText.asStateFlow()

    private val _currentSession = MutableStateFlow<Session?>(null)
    val currentSession: StateFlow<Session?> = _currentSession.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _processingStatus = MutableStateFlow<String?>(null)
    val processingStatus: StateFlow<String?> = _processingStatus.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RecordingForegroundService.LocalBinder
            recordingService = binder.getService()
            bound = true
            recordingService?.onDurationUpdate = { text ->
                _durationText.value = text
            }
            recordingService?.onError = { msg ->
                _error.value = msg
                _isRecording.value = false
            }
            recordingService?.onRecordingStopped = { file, duration ->
                handleRecordingComplete(file, duration)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            recordingService = null
            bound = false
        }
    }

    fun startRecording(sessionTitle: String, consentAcknowledged: Boolean) {
        if (!consentAcknowledged) {
            _error.value = "Consent acknowledgment is required"
            return
        }
        viewModelScope.launch {
            val session = Session(
                title = sessionTitle.ifBlank { "Untitled Session" },
                status = SessionStatus.RECORDING,
                consentAcknowledged = true
            )
            _currentSession.value = session
            _isRecording.value = true
            _isPaused.value = false
            _durationText.value = "00:00"
            _processingStatus.value = null

            sessionRepository.insertSession(session)

            val intent = Intent(app, RecordingForegroundService::class.java)
            app.bindService(intent, connection, Context.BIND_AUTO_CREATE)

            recordingService?.startRecording(session.id)
        }
    }

    fun togglePause() {
        recordingService?.togglePause()
        _isPaused.value = !_isPaused.value
    }

    fun stopRecording() {
        recordingService?.stopRecording()
        if (bound) {
            try { app.unbindService(connection) } catch (_: Exception) {}
            bound = false
        }
    }

    private fun handleRecordingComplete(file: File, durationMs: Long) {
        viewModelScope.launch {
            val sessionId = _currentSession.value?.id ?: return@launch
            val session = _currentSession.value?.copy(
                status = SessionStatus.RECORDED,
                durationMs = durationMs
            )
            if (session != null) {
                sessionRepository.updateSession(session)
                _currentSession.value = session
            }
            _isRecording.value = false
            _isPaused.value = false

            // 🚀 Trigger the closed-loop pipeline: auto-transcribe → auto-summarize → auto-deliver
            _processingStatus.value = "Processing: transcribing..."
            processingPipeline.startProcessing(sessionId, file)
            _processingStatus.value = "Processing complete"
        }
    }

    fun clearError() { _error.value = null }

    override fun onCleared() {
        super.onCleared()
        if (bound) {
            try { app.unbindService(connection) } catch (_: Exception) {}
        }
    }
}
