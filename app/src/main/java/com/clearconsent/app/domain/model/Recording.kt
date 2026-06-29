package com.clearconsent.app.domain.model

import java.util.UUID

data class Recording(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val filePath: String = "",
    val fileSizeBytes: Long = 0L,
    val durationMs: Long = 0L,
    val mimeType: String = "audio/3gpp"
)
