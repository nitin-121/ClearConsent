package com.clearconsent.app.domain.model

data class DailyDigest(
    val date: String, // ISO date: YYYY-MM-DD
    val sessions: List<SummarizedSession> = emptyList()
)

data class SummarizedSession(
    val session: Session,
    val summary: Summary?,
    val recordingDuration: String
)
