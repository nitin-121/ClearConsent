# ClearConsent — Privacy-First Meeting Notes

## Overview
Android app that records audio only on explicit user action with consent flow, transcribes, summarizes, and delivers daily digests.

## Tech Stack
- **Language:** Kotlin 1.9+  
- **UI:** Jetpack Compose + Material 3  
- **Architecture:** MVVM + Clean Architecture  
- **DI:** Hilt  
- **Database:** Room (with Encryption via SQLCipher)  
- **Recording:** MediaRecorder → Foreground Service  
- **Transcription:** On-device SpeechRecognizer (fallback → Whisper API)  
- **Summarization:** Local LLM / API-based  
- **Background:** WorkManager for daily digest  
- **Auth/Biometrics:** BiometricPrompt for app lock  
- **File Encryption:** AES-256 GCM per audio file  
- **Navigation:** Compose Navigation

## Architecture
```
┌─────────────────────────────────────────────────────┐
│                    UI LAYER                          │
│  HomeScreen │ RecordingScreen │ SessionsScreen       │
│  SessionDetail │ TranscriptScreen │ SettingsScreen   │
│  DailyDigestScreen                                   │
├─────────────────────────────────────────────────────┤
│                  VIEWMODEL LAYER                     │
│  RecordingVM │ SessionVM │ TranscriptVM │ SettingsVM │
├─────────────────────────────────────────────────────┤
│                  DOMAIN LAYER                        │
│  Models: Session, Recording, Transcript, Summary     │
│  UseCases: StartRecording, StopRecording,            │
│            TranscribeAudio, SummarizeTranscript,      │
│            GetDailyDigest, ExportSession             │
├─────────────────────────────────────────────────────┤
│                  DATA LAYER                          │
│  Room DB (Sessions, Recordings, Transcripts,          │
│          Summaries, Speakers, ActionItems)            │
│  EncryptedSharedPrefs (Settings)                     │
│  FileStore (Encrypted audio files)                   │
│  Repository implementations                          │
├─────────────────────────────────────────────────────┤
│                  SERVICE LAYER                       │
│  RecordingForegroundService │ TranscriptionService    │
│  SummarizationService │ NotificationHelper           │
│  DailyDigestWorker (WorkManager)                     │
└─────────────────────────────────────────────────────┘
```

## Data Model
```
Session
  - id: UUID (PK)
  - title: String
  - createdAt: Long
  - durationMs: Long
  - status: ENUM (RECORDING, TRANSCRIBING, TRANSCRIBED, SUMMARIZED)
  - consentAcknowledged: Boolean
  - participantCount: Int

Recording
  - id: UUID (PK)
  - sessionId: UUID (FK → Session)
  - filePath: String (encrypted)
  - fileSizeBytes: Long
  - durationMs: Long
  - mimeType: String

Transcript
  - id: UUID (PK)
  - sessionId: UUID (FK → Session)
  - rawText: String
  - language: String
  - segments: List<TranscriptSegment>

TranscriptSegment
  - id: UUID (PK)
  - transcriptId: UUID (FK → Transcript)
  - speakerLabel: String?
  - startMs: Long
  - endMs: Long
  - text: String

Summary
  - id: UUID (PK)
  - sessionId: UUID (FK → Session)
  - keyPoints: List<String>
  - decisions: List<String>
  - actionItems: List<ActionItem>
  - generatedAt: Long

ActionItem
  - id: UUID (PK)
  - summaryId: UUID (FK → Summary)
  - text: String
  - assignee: String?
  - dueDate: String?
  - isCompleted: Boolean
```

## Permissions
- `RECORD_AUDIO` — for recording
- `POST_NOTIFICATIONS` (Android 13+) — recording indicator
- `FOREGROUND_SERVICE` — recording service
- `FOREGROUND_SERVICE_MICROPHONE` (Android 14+)
- `USE_BIOMETRIC` — app lock
- `INTERNET` — if using cloud transcription API

## Privacy Safeguards
1. Consent dialog with confirmation checkbox before EVERY recording
2. Persistent notification with recording timer
3. Red mic icon in status bar while recording
4. All audio files encrypted at rest (AES-256 GCM)
5. File-level encryption key derived from user biometric
6. Delete sessions with cascade deletion of all associated data
7. No background recording — foreground service binds to user action
8. Audio files never leave device unless user explicitly enables cloud sync
9. Daily digest is generated locally; no data sent externally

## UI Flow
```
App Launch → Consent Screen (first run)
  → Home (Recent Sessions + Quick Record FAB)
    → Pre-Record Consent Dialog
      → Recording Screen (timer, waveform, pause/stop)
        → Processing (transcribing)
          → Transcript Screen (editable, searchable)
            → Summary Screen (key points, decisions, actions)
  → Sessions List (search, filter by date)
    → Session Detail (playback, transcript, summary, export, delete)
  → Settings (theme, cloud sync toggle, biometric lock, about)
  → Daily Digest (opens from notification)
```

## Screens
1. **ConsentScreen** — First-launch privacy consent + info
2. **HomeScreen** — Quick record FAB, recent sessions list
3. **PreRecordScreen** — Consent reminder + session title input  
4. **RecordingScreen** — Live timer, waveform visualization, pause/stop
5. **ProcessingScreen** — Transcribing/summarizing with progress
6. **TranscriptScreen** — Full transcript with speaker labels, search
7. **SummaryScreen** — Key points, decisions, action items
8. **SessionsScreen** — All sessions with search/filter
9. **SessionDetailScreen** — Playback, transcript, summary, actions
10. **SettingsScreen** — Preferences, security, cloud sync, about
11. **DailyDigestScreen** — Aggregated summaries by date
