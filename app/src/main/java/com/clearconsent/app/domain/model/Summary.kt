package com.clearconsent.app.domain.model

import java.util.UUID

data class Summary(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val keyPoints: List<String> = emptyList(),
    val decisions: List<String> = emptyList(),
    val actionItems: List<ActionItem> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis()
)

data class ActionItem(
    val id: String = UUID.randomUUID().toString(),
    val summaryId: String,
    val text: String = "",
    val assignee: String? = null,
    val dueDate: String? = null,
    val isCompleted: Boolean = false
)
