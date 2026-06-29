package com.clearconsent.app.ui.screens.recording

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clearconsent.app.ui.components.ConsentCheckbox
import com.clearconsent.app.ui.theme.RecordingRed
import com.clearconsent.app.ui.theme.StatusTranscribing
import com.clearconsent.app.viewmodel.RecordingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val durationText by viewModel.durationText.collectAsState()
    val error by viewModel.error.collectAsState()
    val processingStatus by viewModel.processingStatus.collectAsState()

    var sessionTitle by remember { mutableStateOf("") }
    var consentChecked by remember { mutableStateOf(false) }
    var showConsentStep by remember { mutableStateOf(true) }

    // Error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isRecording) "Recording" else if (showConsentStep) "New Recording" else "Ready")
                },
                navigationIcon = {
                    if (!isRecording) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isRecording || processingStatus != null) {
                // Recording / Processing state
                RecordingActiveContent(
                    isRecording = isRecording,
                    isPaused = isPaused,
                    durationText = durationText,
                    processingStatus = processingStatus,
                    onPauseResume = { viewModel.togglePause() },
                    onStop = { viewModel.stopRecording() }
                )
            } else if (showConsentStep) {
                // Pre-recording consent form
                ConsentStepContent(
                    sessionTitle = sessionTitle,
                    onTitleChange = { sessionTitle = it },
                    consentChecked = consentChecked,
                    onConsentChanged = { consentChecked = it },
                    onStart = {
                        viewModel.startRecording(sessionTitle, consentChecked)
                        showConsentStep = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ConsentStepContent(
    sessionTitle: String,
    onTitleChange: (String) -> Unit,
    consentChecked: Boolean,
    onConsentChanged: (Boolean) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Shield,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Record with Consent",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This app records audio ONLY when you explicitly tap \"Start Recording\". All participants must be informed and consent before recording begins.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = sessionTitle,
            onValueChange = onTitleChange,
            label = { Text("Session title (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(16.dp))

        ConsentCheckbox(
            isChecked = consentChecked,
            onCheckedChange = onConsentChanged
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStart,
            enabled = consentChecked,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RecordingRed)
        ) {
            Icon(Icons.Default.Mic, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Recording", fontSize = 16.sp)
        }
    }
}

@Composable
private fun RecordingActiveContent(
    isRecording: Boolean,
    isPaused: Boolean,
    durationText: String,
    processingStatus: String?,
    onPauseResume: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (processingStatus != null) {
            // Processing state (after recording, before done)
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = StatusTranscribing
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = processingStatus,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        } else {
            // Recording state
            val indicatorColor by animateColorAsState(
                targetValue = if (isPaused) StatusTranscribing else RecordingRed,
                label = "indicator"
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isPaused) "PAUSED" else "RECORDING",
                color = indicatorColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = durationText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Pause/Resume button
                FilledTonalButton(
                    onClick = onPauseResume,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        modifier = Modifier.size(28.dp)
                    )
                }
                // Stop button
                Button(
                    onClick = onStop,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = RecordingRed)
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
