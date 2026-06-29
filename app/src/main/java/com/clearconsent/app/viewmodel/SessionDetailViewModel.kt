package com.clearconsent.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearconsent.app.domain.model.*
import com.clearconsent.app.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    private val _transcript = MutableStateFlow<Transcript?>(null)
    val transcript: StateFlow<Transcript?> = _transcript.asStateFlow()

    private val _summary = MutableStateFlow<Summary?>(null)
    val summary: StateFlow<Summary?> = _summary.asStateFlow()

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _session.value = sessionRepository.getSessionById(sessionId)
            _transcript.value = sessionRepository.getTranscript(sessionId)
            _summary.value = sessionRepository.getSummary(sessionId)
        }
    }

    fun deleteSession() {
        viewModelScope.launch {
            _session.value?.let { sessionRepository.deleteSession(it.id) }
            _session.value = null
        }
    }
}
