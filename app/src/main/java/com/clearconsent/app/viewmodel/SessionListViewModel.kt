package com.clearconsent.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearconsent.app.domain.model.Session
import com.clearconsent.app.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionListViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val sessions: StateFlow<List<Session>> = sessionRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun deleteSession(sessionId: String) {
        viewModelScope.launch { sessionRepository.deleteSession(sessionId) }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    val filteredSessions: StateFlow<List<Session>> = combine(sessions, _searchQuery) { all, query ->
        if (query.isBlank()) all
        else all.filter { it.title.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
