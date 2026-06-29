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
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DailyDigestViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _digests = MutableStateFlow<List<DailyDigest>>(emptyList())
    val digests: StateFlow<List<DailyDigest>> = _digests.asStateFlow()

    init { loadDigests() }

    fun loadDigests() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val todayStart = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val todayEnd = todayStart + 86400000L

            val sessions = mutableListOf<SummarizedSession>()
            sessionRepository.getSessionsByDate(todayStart, todayEnd).collect { sessionList ->
                sessions.clear()
                for (session in sessionList) {
                    val summary = sessionRepository.getSummary(session.id)
                    val durMinutes = session.durationMs / 60000
                    val durSecs = (session.durationMs % 60000) / 1000
                    val durStr = if (durMinutes > 0) "${durMinutes}m ${durSecs}s" else "${durSecs}s"
                    sessions.add(SummarizedSession(session, summary, durStr))
                }
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(todayStart)
                _digests.value = listOf(DailyDigest(date = dateStr, sessions = sessions.toList()))
            }
        }
    }
}
