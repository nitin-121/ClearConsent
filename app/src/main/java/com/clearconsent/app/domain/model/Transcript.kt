package com.clearconsent.app.domain.model

import java.util.UUID

data class Transcript(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val rawText: String = "",
    val language: String = "en",
    val segments: List<TranscriptSegment> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class TranscriptSegment(
    val id: String = UUID.randomUUID().toString(),
    val transcriptId: String,
    val speakerLabel: String? = null,
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val text: String = ""
)
