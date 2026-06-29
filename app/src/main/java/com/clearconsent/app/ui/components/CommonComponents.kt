package com.clearconsent.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clearconsent.app.domain.model.SessionStatus
import com.clearconsent.app.ui.theme.*

@Composable
fun RecordingIndicator(isRecording: Boolean, isPaused: Boolean = false, modifier: Modifier = Modifier) {
    val color by animateColorAsState(
        targetValue = if (isRecording && !isPaused) RecordingRed else if (isPaused) StatusTranscribing else Surface,
        label = "recordingColor"
    )
    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = if (isPaused) "PAUSED" else "REC",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun StatusBadge(status: SessionStatus, modifier: Modifier = Modifier) {
    val (text, color) = when (status) {
        SessionStatus.PENDING -> "Pending" to StatusTranscribing
        SessionStatus.RECORDING -> "Recording" to RecordingRed
        SessionStatus.PAUSED -> "Paused" to StatusTranscribing
        SessionStatus.RECORDED -> "Recorded" to StatusTranscribing
        SessionStatus.TRANSCRIBING -> "Transcribing..." to StatusTranscribing
        SessionStatus.TRANSCRIBED -> "Transcribed" to StatusTranscribed
        SessionStatus.SUMMARIZING -> "Summarizing..." to StatusTranscribing
        SessionStatus.SUMMARIZED -> "Summarized" to StatusSummarized
        SessionStatus.ERROR -> "Error" to Error
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ConsentCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "I confirm all participants have consented to this recording",
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
