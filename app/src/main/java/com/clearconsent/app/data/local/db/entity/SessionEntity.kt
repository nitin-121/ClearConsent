package com.clearconsent.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.clearconsent.app.domain.model.Session
import com.clearconsent.app.domain.model.SessionStatus

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val durationMs: Long,
    val status: String,
    val consentAcknowledged: Boolean,
    val participantCount: Int
) {
    fun toDomain(): Session = Session(
        id = id,
        title = title,
        createdAt = createdAt,
        durationMs = durationMs,
        status = try { SessionStatus.valueOf(status) } catch (e: Exception) { SessionStatus.ERROR },
        consentAcknowledged = consentAcknowledged,
        participantCount = participantCount
    )

    companion object {
        fun fromDomain(session: Session): SessionEntity = SessionEntity(
            id = session.id,
            title = session.title,
            createdAt = session.createdAt,
            durationMs = session.durationMs,
            status = session.status.name,
            consentAcknowledged = session.consentAcknowledged,
            participantCount = session.participantCount
        )
    }
}
