package com.clearconsent.app.data.local.db.dao

import androidx.room.*
import com.clearconsent.app.data.local.db.entity.RecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String): RecordingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recording: RecordingEntity)

    @Delete
    suspend fun delete(recording: RecordingEntity)

    @Query("DELETE FROM recordings WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)
}
