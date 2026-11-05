# Orvia Test & Verification Guide

This document details the test checklist, dead-button verification sweep, icon and string resource audits, and live provider testing steps.

---

## 1. Automated Unit Tests

Automated unit tests run on every push in CI (`.github/workflows/build-apk.yml`) via `./gradlew test`:

| Test Suite | Class | Coverage |
| :--- | :--- | :--- |
| **SSE Stream Parser** | `SseStreamParserTest` | OpenAI delta parser, Gemini stream parser, completion events, error chunks |
| **Patch Applier & Diff** | `PatchApplierTest` | Line-by-line diff calculation (added, deleted, unchanged), in-place file patching, diff statistics |
| **Request Builders** | `RequestBuilderTest` | Gemini payload generator, Groq payload generator, OpenRouter payload generator, Custom provider headers & parameters |
| **OpenRouter Filter** | `OpenRouterFilterTest` | Free model filtering logic (`:free` suffix & zero pricing) |
| **Fallback Order** | `FallbackOrderTest` | Automatic provider re-routing on rate limits (429) and server errors (5xx) |

---

## 2. Dead-Button Verification Sweep

Every interactive element on every screen has been audited and mapped to real operations:

### Chat Screen
- [x] **Navigation Drawer Menu Button**: Opens the Chat History drawer sheet.
- [x] **Mode Segmented Toggle**: Switches active mode between *Ask Anything*, *Builder*, and *Master Prompt*.
- [x] **New Chat Button (+)**: Clears current session, resets state, and opens a new chat session.
- [x] **Model Chip**: Displays current active model; displays guidance toast directing to Settings for model configuration.
- [x] **Live Summary Bar**: Tapping opens the live `LiveWorkSummarySheet` timeline showing active steps, line diffs, and errors.
- [x] **Suggestion Cards**: Tapping inserts suggestion text directly into the input bar and submits.
- [x] **Scroll-to-Bottom FAB**: Smoothly scrolls the message list down to the newest message.
- [x] **Message Long-Press Menu**:
  - [x] *Copy*: Copies markdown text to system clipboard and displays a "Copied" toast.
  - [x] *Edit & Resend*: Populates text box with message content for quick editing.
  - [x] *Share*: Launches Android system share sheet.
- [x] **Error Card Retry Button**: Re-submits the failed prompt to the provider or fallback chain.
- [x] **Code Block Copy Button**: Copies code snippet to clipboard with visual confirmation.
- [x] **Inline Image Actions**:
  - [x] *Save to Gallery*: Saves image bytes to MediaStore.
  - [x] *Regenerate*: Triggers a re-prompt for image creation.

### Input Bar
- [x] **Plus (+) Menu Button**:
  - [x] *Attach Images / Files*: Launches system file/image picker.
  - [x] *Web Search Toggle*: Toggles live search grounding parameter.
  - [x] *Thinking Mode Toggle*: Enables reasoning tokens and display of the collapsible Thinking card.
- [x] **Clear (X) Button**: Clears text input when non-empty.
- [x] **Mic Button**: Starts Android speech recognition (English, Hindi, Hinglish).
- [x] **Send Button**: Disabled when input is empty; submits message when text or attachment is present.
- [x] **Stop Streaming Button**: Replaces Send while streaming is active; cancels the active SSE coroutine.

### Builder Mode
- [x] **Adaptive Question Bottom Sheet**:
  - [x] *Single Choice Chips*: Selects single option.
  - [x] *Multi Choice Chips*: Selects/unselects multiple options.
  - [x] *Free Text Field*: Captures custom user input.
  - [x] *Next Button*: Submits answer and requests next adaptive question.
- [x] **File Cards**:
  - [x] *Open*: Launches `BuilderCodeViewerDialog` with syntax highlighting and file tree navigation.
  - [x] *Download*: Exports file or entire project ZIP to phone `Download/` folder.
  - [x] *Share*: Shares code via Android Intent.
- [x] **Commit & Push Button**: Connects to GitHub API using user's encrypted PAT, creates repo if needed, and pushes all files.
- [x] **Error Fix Button**: Feeds build/runtime errors back to AI to generate a patch.

### Master Prompt Mode
- [x] **Copy Action**: Copies master prompt markdown.
- [x] **Download Action**: Saves `chat-title.md` to device storage.
- [x] **Share Action**: Opens system share chooser.
- [x] **Refine Action**: Re-opens adaptive interview for modifications.
- [x] **In-Place Diff Viewer**: Shows red (removed) and green (added) lines before saving; includes *Accept* and *Undo* buttons.

### Settings Screen
- [x] **Provider Tabs & Fields**: Allows input of Gemini, Groq, OpenRouter, and GitHub keys with Keystore encryption.
- [x] **Test Connection Buttons**: Sends live probe to provider and displays real HTTP status code and response time.
- [x] **Custom Provider Editor**: Add, edit, toggle, and delete custom OpenAI-compatible endpoints.
- [x] **Fallback Order Dialog**: Drag-and-drop or up/down ordering of fallback provider list.
- [x] **Chat Settings**: Default model selector, global system prompt editor, font size slider, typing speed slider.
- [x] **Memory List**: Add new memory item, edit existing items, swipe or tap delete.
- [x] **GitHub Settings**: PAT entry, visibility toggle (Public/Private), and "Test Token" button.
- [x] **Appearance Settings**: Theme selection (Light / Dark / System), animation toggle.
- [x] **Sound & Haptics**: Vibration toggle, sound effect toggle, voice language dropdown.
- [x] **Data Management**: Clear all chats, clear all data with safety confirmation dialogs.

---

## 3. Icon and Text Resource Audit

- **100% VectorDrawables**: All icons are XML VectorDrawables in `res/drawable/` using clean SVG path data. Zero emoji, zero raster PNGs, zero web fonts.
- **100% strings.xml**: All user-facing strings are strictly defined in `res/values/strings.xml`. Zero hardcoded user strings.
- **Screen Width Adaptability**: Tested layout constraints for widths from 360dp to 430dp. Text views use standard elipsize, flexible wrapping, and responsive Compose modifiers to eliminate clipping.

---

## 4. Live API Check Procedure

1. **Google Gemini**:
   - Obtain key from Google AI Studio (`aistudio.google.com`).
   - Paste in *Settings -> Providers -> Gemini*.
   - Tap *Test Connection* -> Confirms HTTP 200.
   - Send prompt: "Explain quantum superposition in Hindi" -> Receives streamed Hindi response.
2. **Groq**:
   - Obtain key from `console.groq.com`.
   - Paste in *Settings -> Providers -> Groq*.
   - Tap *Test Connection* -> Confirms HTTP 200.
   - Send prompt -> Receives blazing-fast streamed completion.
3. **OpenRouter**:
   - Obtain key from `openrouter.ai`.
   - Toggle "Free only" ON.
   - Test connection and verify free model list loads.
4. **Custom Provider**:
   - Enter base URL (e.g., `https://api.together.xyz/v1`), model name, and API key.
   - Tap *Test Connection* -> Real HTTP verification.
