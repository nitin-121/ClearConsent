package com.clearconsent.app.data.local.db.dao

import androidx.room.*
import com.clearconsent.app.data.local.db.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {
    @Query("SELECT * FROM summaries WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String): SummaryEntity?

    @Query("SELECT * FROM summaries WHERE generatedAt >= :startOfDay AND generatedAt < :endOfDay")
    fun getSummariesByDate(startOfDay: Long, endOfDay: Long): Flow<List<SummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: SummaryEntity)

    @Update
    suspend fun update(summary: SummaryEntity)

    @Delete
    suspend fun delete(summary: SummaryEntity)

    @Query("DELETE FROM summaries WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)
}
