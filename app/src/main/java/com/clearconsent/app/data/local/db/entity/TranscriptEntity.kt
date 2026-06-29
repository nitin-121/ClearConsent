package com.clearconsent.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.clearconsent.app.domain.model.Transcript

@Entity(
    tableName = "transcripts",
    foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class TranscriptEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val rawText: String,
    val language: String,
    val createdAt: Long
) {
    fun toDomain(): Transcript = Transcript(id = id, sessionId = sessionId, rawText = rawText, language = language, createdAt = createdAt)
    companion object {
        fun fromDomain(t: Transcript): TranscriptEntity = TranscriptEntity(id = t.id, sessionId = t.sessionId, rawText = t.rawText, language = t.language, createdAt = t.createdAt)
    }
}
