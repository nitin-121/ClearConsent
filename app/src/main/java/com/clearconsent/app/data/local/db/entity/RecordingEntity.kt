package com.clearconsent.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.clearconsent.app.domain.model.Recording

@Entity(
    tableName = "recordings",
    foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class RecordingEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val durationMs: Long,
    val mimeType: String
) {
    fun toDomain(): Recording = Recording(
        id = id, sessionId = sessionId, filePath = filePath,
        fileSizeBytes = fileSizeBytes, durationMs = durationMs, mimeType = mimeType
    )
    companion object {
        fun fromDomain(r: Recording): RecordingEntity = RecordingEntity(
            id = r.id, sessionId = r.sessionId, filePath = r.filePath,
            fileSizeBytes = r.fileSizeBytes, durationMs = r.durationMs, mimeType = r.mimeType
        )
    }
}
