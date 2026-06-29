package com.clearconsent.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.clearconsent.app.domain.model.TranscriptSegment

@Entity(
    tableName = "transcript_segments",
    foreignKeys = [ForeignKey(
        entity = TranscriptEntity::class,
        parentColumns = ["id"],
        childColumns = ["transcriptId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("transcriptId")]
)
data class TranscriptSegmentEntity(
    @PrimaryKey val id: String,
    val transcriptId: String,
    val speakerLabel: String?,
    val startMs: Long,
    val endMs: Long,
    val text: String
) {
    fun toDomain(): TranscriptSegment = TranscriptSegment(id, transcriptId, speakerLabel, startMs, endMs, text)
    companion object {
        fun fromDomain(s: TranscriptSegment): TranscriptSegmentEntity = TranscriptSegmentEntity(s.id, s.transcriptId, s.speakerLabel, s.startMs, s.endMs, s.text)
    }
}
