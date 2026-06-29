package com.clearconsent.app.data.local.db.dao

import androidx.room.*
import com.clearconsent.app.data.local.db.entity.TranscriptEntity
import com.clearconsent.app.data.local.db.entity.TranscriptSegmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {
    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String): TranscriptEntity?

    @Query("SELECT * FROM transcripts WHERE rawText LIKE '%' || :query || '%'")
    fun searchTranscripts(query: String): Flow<List<TranscriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transcript: TranscriptEntity)

    @Update
    suspend fun update(transcript: TranscriptEntity)

    @Delete
    suspend fun delete(transcript: TranscriptEntity)

    @Query("DELETE FROM transcripts WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)

    // Segments
    @Query("SELECT * FROM transcript_segments WHERE transcriptId = :transcriptId ORDER BY startMs ASC")
    suspend fun getSegments(transcriptId: String): List<TranscriptSegmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<TranscriptSegmentEntity>)

    @Query("DELETE FROM transcript_segments WHERE transcriptId = :transcriptId")
    suspend fun deleteSegmentsByTranscriptId(transcriptId: String)
}
