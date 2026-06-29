package com.clearconsent.app.data.repository

import com.clearconsent.app.data.local.db.dao.SessionDao
import com.clearconsent.app.data.local.db.dao.RecordingDao
import com.clearconsent.app.data.local.db.dao.TranscriptDao
import com.clearconsent.app.data.local.db.dao.SummaryDao
import com.clearconsent.app.data.local.db.entity.*
import com.clearconsent.app.domain.model.*
import com.clearconsent.app.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val recordingDao: RecordingDao,
    private val transcriptDao: TranscriptDao,
    private val summaryDao: SummaryDao
) : SessionRepository {

    override fun getAllSessions(): Flow<List<Session>> =
        sessionDao.getAllSessions().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getSessionById(id: String): Session? =
        sessionDao.getSessionById(id)?.toDomain()

    override fun getSessionsByDate(startOfDay: Long, endOfDay: Long): Flow<List<Session>> =
        sessionDao.getSessionsByDate(startOfDay, endOfDay).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertSession(session: Session) =
        sessionDao.insert(SessionEntity.fromDomain(session))

    override suspend fun updateSession(session: Session) =
        sessionDao.update(SessionEntity.fromDomain(session))

    override suspend fun deleteSession(id: String) =
        sessionDao.deleteById(id)

    override suspend fun getTranscript(sessionId: String): Transcript? {
        val entity = transcriptDao.getBySessionId(sessionId) ?: return null
        val segments = transcriptDao.getSegments(entity.id).map { it.toDomain() }
        return entity.toDomain().copy(segments = segments)
    }

    override suspend fun saveTranscript(transcript: Transcript) {
        transcriptDao.insert(TranscriptEntity.fromDomain(transcript))
        transcriptDao.deleteSegmentsByTranscriptId(transcript.id)
        transcriptDao.insertSegments(transcript.segments.map { TranscriptSegmentEntity.fromDomain(it) })
    }

    override suspend fun getSummary(sessionId: String): Summary? =
        summaryDao.getBySessionId(sessionId)?.toDomain()

    override suspend fun saveSummary(summary: Summary) =
        summaryDao.insert(SummaryEntity.fromDomain(summary))
}
