# Orvia - Android Chat + Builder + Master Prompt Generator

Orvia is a privacy-first, autonomous Android chat assistant, project builder, and master prompt generator built with modern Jetpack Compose and Material 3.

## ✨ Features

- **Ask Anything**: Worldwide multilingual AI chat (Hindi, English, Hinglish) with streaming responses, thinking mode, grounding web search, image/document inputs, and speech-to-text.
- **Builder**: Adaptive 12–20 question interview for apps and websites. Generates full projects, provides an in-app file tree and syntax-highlighted code viewer, ZIP exports, and direct GitHub repository creation and pushing.
- **Master Prompt Generator**: Guided interview culminating in an English `.md` master prompt file with in-place diff edits (red/green lines) and undo version history.
- **Multi-Provider AI**: Google Gemini, Groq, OpenRouter, and unlimited custom OpenAI-compatible endpoints with auto-failover and latency tracking.
- **Security & Privacy**: No login, no telemetry, no middleman backend. All API keys and tokens are encrypted locally with hardware-backed Android Keystore.
- **Home Widget**: One-tap quick launcher for instant conversations.

## 🛠 Tech Stack

- **Platform**: Android (Min SDK 31, Target SDK 35)
- **Language**: Kotlin 2.0.21
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Repository Pattern
- **Dependency Injection**: Hilt
- **Local Storage**: Room Database + DataStore Preferences
- **Security**: Android Keystore AES-256 GCM
- **Networking**: OkHttp 4.12 SSE Streaming + Retrofit / Gson

## 📦 Building from Source

```bash
# Clone the repository
git clone https://github.com/mdshahiid09-bit/orvia.git
cd orvia

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

The compiled APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

## 📥 Installation

1. Navigate to the [Releases](https://github.com/mdshahiid09-bit/orvia/releases) section.
2. Download `Orvia-v1.0.0-debug.apk`.
3. Tap to install on your Android device (Android 12+).
