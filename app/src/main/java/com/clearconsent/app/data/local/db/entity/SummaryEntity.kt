package com.clearconsent.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.clearconsent.app.domain.model.Summary

@Entity(
    tableName = "summaries",
    foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class SummaryEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val keyPointsJson: String, // JSON array
    val decisionsJson: String, // JSON array
    val actionItemsJson: String, // JSON array
    val generatedAt: Long
) {
    fun toDomain(): Summary = Summary(id, sessionId, emptyList(), emptyList(), emptyList(), generatedAt)
    companion object {
        fun fromDomain(s: Summary): SummaryEntity = SummaryEntity(s.id, s.sessionId, "[]", "[]", "[]", s.generatedAt)
    }
}
