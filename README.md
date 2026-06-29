# ClearConsent 📝🔒

**Privacy-First Meeting Notes for Android**

ClearConsent is a privacy-first Android app that records audio ONLY when you explicitly tap "Start" and all participants have consented. It transcribes, summarizes, and delivers daily digests — all with encryption and no hidden recording.

## Features

### Core
- ✅ **Explicit recording** — Audio records only on manual "Start Recording" tap
- ✅ **Consent flow** — Mandatory consent acknowledgment before each session
- ✅ **Foreground service** — Persistent notification with timer while recording
- ✅ **One-tap pause/stop** — Full control during recording
- ✅ **On-device transcription** — Convert speech to text (fallback to API)
- ✅ **Smart summaries** — Key points, decisions, and action items per session
- ✅ **Daily digest** — Aggregated summaries delivered via notification
- ✅ **Transcript search** — Full-text search across all transcripts

### Privacy & Security
- 🔒 **Encrypted at rest** — AES-256 GCM on all audio files
- 🔒 **Biometric app lock** — Optional fingerprint/PIN gate
- 🔒 **No background recording** — Ever. Foreground service only.
- 🔒 **Cascade delete** — Delete a session = removes audio, transcript, summary
- 🔒 **No cloud by default** — All data stays local unless you enable sync
- 🔒 **Open source** — Fully auditable codebase

## Architecture

```
UI Layer (Jetpack Compose + Material 3)
    ↕ ViewModels
Domain Layer (UseCases + Models)
    ↕ Repositories
Data Layer (Room DB + EncryptedStorage)
Service Layer (Foreground Service + WorkManager)
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room (SQLite) |
| Recording | MediaRecorder + Foreground Service |
| Transcription | Android SpeechRecognizer / Whisper API |
| Background | WorkManager |
| Encryption | AES-256 GCM |
| Biometrics | AndroidX Biometric |

## Permissions

| Permission | Why |
|-----------|-----|
| `RECORD_AUDIO` | To record meeting audio |
| `POST_NOTIFICATIONS` | Recording indicator (Android 13+) |
| `FOREGROUND_SERVICE` | Keep recording alive |
| `FOREGROUND_SERVICE_MICROPHONE` | Mic access in foreground (Android 14+) |
| `USE_BIOMETRIC` | Optional app lock |

## Screens

1. **Home** — Recent sessions + quick record FAB
2. **Pre-Record Consent** — Consent reminder + title input
3. **Recording** — Live timer, waveform, pause/stop
4. **Processing** — Transcribing/summarizing progress
5. **Transcript** — Full transcript with speaker labels + search
6. **Summary** — Key points, decisions, action items
7. **Sessions** — All sessions with search/filter by date
8. **Session Detail** — Playback, transcript, summary, export, delete
9. **Settings** — Theme, biometric lock, cloud sync toggle
10. **Daily Digest** — Aggregated summaries by date

## Privacy Promise

1. **No recording without consent** — Each session requires explicit consent acknowledgment
2. **No hidden recording** — Persistent notification + red mic icon while recording
3. **No background recording** — Foreground service only, bound to user action
4. **No data sharing** — All processing on-device; cloud sync is opt-in
5. **Full deletion** — Delete any session and all its data is gone forever

## Getting Started

1. Clone the repo
2. Open in Android Studio (Hedgehog or later)
3. Sync Gradle
4. Run on device/emulator (API 26+)
5. Grant microphone and notification permissions
6. Tap "Start Recording" to begin

## License

MIT License — see [LICENSE](LICENSE)

## Built With

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt](https://dagger.dev/hilt/)
- [Room](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
