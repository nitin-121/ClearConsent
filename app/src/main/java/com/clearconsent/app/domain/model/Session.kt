package com.clearconsent.app.domain.model

import java.util.UUID

data class Session(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val status: SessionStatus = SessionStatus.PENDING,
    val consentAcknowledged: Boolean = false,
    val participantCount: Int = 1
)

enum class SessionStatus {
    PENDING,
    RECORDING,
    PAUSED,
    RECORDED,
    TRANSCRIBING,
    TRANSCRIBED,
    SUMMARIZING,
    SUMMARIZED,
    ERROR
}
