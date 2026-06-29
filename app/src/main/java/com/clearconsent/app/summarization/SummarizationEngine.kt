package com.clearconsent.app.summarization

import com.clearconsent.app.domain.model.ActionItem
import com.clearconsent.app.domain.model.Summary
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummarizationEngine @Inject constructor() {

    data class SummarizationResult(
        val keyPoints: List<String>,
        val decisions: List<String>,
        val actionItems: List<ActionItem>
    )

    /**
     * Generates a structured summary from transcript text.
     * Uses NLP heuristics for now; can be replaced with LLM API later.
     */
    fun summarize(sessionId: String, transcriptText: String): Summary {
        val lines = transcriptText.lines().filter { it.isNotBlank() }
        val keyPoints = extractKeyPoints(lines)
        val decisions = extractDecisions(lines)
        val actionItems = extractActionItems(lines, sessionId)

        return Summary(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            keyPoints = keyPoints,
            decisions = decisions,
            actionItems = actionItems,
            generatedAt = System.currentTimeMillis()
        )
    }

    private fun extractKeyPoints(lines: List<String>): List<String> {
        // Heuristic: pick sentences with keywords
        val keywords = listOf("key", "important", "main", "critical", "goal", "objective", "priority")
        return lines.filter { line ->
            keywords.any { keyword -> line.contains(keyword, ignoreCase = true) }
        }.take(5).ifEmpty { lines.take(3) }
    }

    private fun extractDecisions(lines: List<String>): List<String> {
        val decisionWords = listOf("decided", "decision", "agreed", "approved", "confirmed", "resolved")
        return lines.filter { line ->
            decisionWords.any { word -> line.contains(word, ignoreCase = true) }
        }.take(5)
    }

    private fun extractActionItems(lines: List<String>, sessionId: String): List<ActionItem> {
        val actionWords = listOf("action", "todo", "to do", "assign", "deadline", "follow up", "need to", "will")
        return lines.filter { line ->
            actionWords.any { word -> line.contains(word, ignoreCase = true) }
        }.map { text ->
            ActionItem(
                id = UUID.randomUUID().toString(),
                summaryId = sessionId,
                text = text.trim(),
                assignee = extractAssignee(text),
                dueDate = null,
                isCompleted = false
            )
        }.take(10)
    }

    private fun extractAssignee(text: String): String? {
        val patterns = listOf("@\\w+", "assignee:\\s*(\\w+)", "owner:\\s*(\\w+)")
        for (pattern in patterns) {
            val match = Regex(pattern, RegexOption.IGNORE_CASE).find(text)
            if (match != null) return match.value
        }
        return null
    }
}
