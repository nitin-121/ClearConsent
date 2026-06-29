package com.clearconsent.app.ui.screens.dailydigest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clearconsent.app.domain.model.DailyDigest
import com.clearconsent.app.domain.model.SummarizedSession
import com.clearconsent.app.ui.components.EmptyState
import com.clearconsent.app.ui.theme.*
import com.clearconsent.app.viewmodel.DailyDigestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyDigestScreen(
    onSessionClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DailyDigestViewModel = hiltViewModel()
) {
    val digests by viewModel.digests.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Digest") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (digests.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(message = "No sessions today. Record a meeting to see your daily digest.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (digest in digests) {
                    item {
                        Text(
                            text = "📅 ${digest.date}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${digest.sessions.size} session(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    items(digest.sessions) { summarized ->
                        DigestSessionCard(
                            summarized = summarized,
                            onClick = { onSessionClick(summarized.session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DigestSessionCard(
    summarized: SummarizedSession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = summarized.session.title.ifBlank { "Untitled Session" },
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Duration: ${summarized.recordingDuration}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (summarized.summary != null) {
                Spacer(modifier = Modifier.height(8.dp))
                for (point in summarized.summary.keyPoints.take(3)) {
                    Text(
                        text = "• $point",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                if (summarized.summary.actionItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summarized.summary.actionItems.size} action item(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                }
            }
        }
    }
}
