package com.clearconsent.app.domain.repository

import com.clearconsent.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    suspend fun getSessionById(id: String): Session?
    fun getSessionsByDate(startOfDay: Long, endOfDay: Long): Flow<List<Session>>
    suspend fun insertSession(session: Session)
    suspend fun updateSession(session: Session)
    suspend fun deleteSession(id: String)
    suspend fun getTranscript(sessionId: String): Transcript?
    suspend fun saveTranscript(transcript: Transcript)
    suspend fun getSummary(sessionId: String): Summary?
    suspend fun saveSummary(summary: Summary)
}
