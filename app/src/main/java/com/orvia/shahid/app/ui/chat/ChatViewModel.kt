package com.orvia.shahid.app.ui.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orvia.shahid.app.data.local.datastore.PreferencesManager
import com.orvia.shahid.app.data.repository.BuilderRepository
import com.orvia.shahid.app.data.repository.ChatRepository
import com.orvia.shahid.app.data.repository.MasterPromptRepository
import com.orvia.shahid.app.data.repository.MemoryRepository
import com.orvia.shahid.app.data.repository.ProviderManager
import com.orvia.shahid.app.domain.diff.DiffResult
import com.orvia.shahid.app.domain.model.AdaptiveQuestion
import com.orvia.shahid.app.domain.model.BuilderProject
import com.orvia.shahid.app.domain.model.ChatMessage
import com.orvia.shahid.app.domain.model.ChatMode
import com.orvia.shahid.app.domain.model.ChatSession
import com.orvia.shahid.app.domain.model.MasterPromptVersion
import com.orvia.shahid.app.domain.model.MessageRole
import com.orvia.shahid.app.domain.model.StepStatus
import com.orvia.shahid.app.domain.model.WorkStep
import com.orvia.shahid.app.service.AiGenerationService
import com.orvia.shahid.app.ui.common.SoundHapticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val currentChat: ChatSession? = null,
    val currentMode: ChatMode = ChatMode.ASK_ANYTHING,
    val messages: List<ChatMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val streamingText: String = "",
    val streamingReasoning: String = "",
    val isThinking: Boolean = false,
    val thinkingSeconds: Int = 0,
    val webSearchEnabled: Boolean = false,
    val thinkingEnabled: Boolean = false,
    val activeModelName: String = "Gemini",
    val activeQuestion: AdaptiveQuestion? = null,
    val activeProject: BuilderProject? = null,
    val activeMasterPrompt: MasterPromptVersion? = null,
    val pendingDiff: Pair<String, DiffResult>? = null,
    val liveSteps: List<WorkStep> = emptyList(),
    val errorMessage: String? = null,
    val currentInputDraft: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatRepository: ChatRepository,
    private val providerManager: ProviderManager,
    private val builderRepository: BuilderRepository,
    private val masterPromptRepository: MasterPromptRepository,
    private val memoryRepository: MemoryRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val allChats: StateFlow<List<ChatSession>> = chatRepository.getAllChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val soundHapticsHelper = SoundHapticsHelper(context)
    private var streamingJob: Job? = null
    private var thinkingTimerJob: Job? = null

    // Interview transcript tracking
    private val builderQaList = mutableListOf<Pair<String, String>>()
    private val masterPromptQaList = mutableListOf<Pair<String, String>>()

    init {
        viewModelScope.launch {
            preferencesManager.defaultModel.collect { model ->
                _uiState.value = _uiState.value.copy(activeModelName = model)
            }
        }
        startNewChat(ChatMode.ASK_ANYTHING)
    }

    fun setChatMode(mode: ChatMode) {
        if (_uiState.value.currentMode != mode) {
            _uiState.value = _uiState.value.copy(currentMode = mode)
            startNewChat(mode)
        }
    }

    fun startNewChat(mode: ChatMode = _uiState.value.currentMode) {
        viewModelScope.launch {
            val title = when (mode) {
                ChatMode.ASK_ANYTHING -> "Ask Anything"
                ChatMode.BUILDER -> "Builder Project"
                ChatMode.MASTER_PROMPT -> "Master Prompt"
            }
            val newId = chatRepository.createNewChat(mode, title)
            selectChat(newId)
        }
    }

    fun selectChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.getChatById(chatId).first()?.let { session ->
                _uiState.value = _uiState.value.copy(
                    currentChat = session,
                    currentMode = session.mode,
                    currentInputDraft = session.draft,
                    errorMessage = null
                )
                loadMessages(chatId)
            }
        }
    }

    private fun loadMessages(chatId: String) {
        viewModelScope.launch {
            chatRepository.getMessagesForChat(chatId).collect { msgList ->
                _uiState.value = _uiState.value.copy(messages = msgList)
            }
        }
    }

    fun toggleWebSearch() {
        _uiState.value = _uiState.value.copy(webSearchEnabled = !_uiState.value.webSearchEnabled)
    }

    fun toggleThinking() {
        _uiState.value = _uiState.value.copy(thinkingEnabled = !_uiState.value.thinkingEnabled)
    }

    fun onDraftChange(draft: String) {
        _uiState.value = _uiState.value.copy(currentInputDraft = draft)
        _uiState.value.currentChat?.id?.let { id ->
            viewModelScope.launch { chatRepository.saveDraft(id, draft) }
        }
    }

    fun sendMessage(inputContent: String, images: List<String> = emptyList()) {
        val content = inputContent.trim()
        if (content.isEmpty() && images.isEmpty()) return

        val chatId = _uiState.value.currentChat?.id ?: return
        val currentMode = _uiState.value.currentMode

        // Haptics & Sound
        viewModelScope.launch {
            val haptics = preferencesManager.hapticsEnabled.first()
            val sound = preferencesManager.soundEnabled.first()
            soundHapticsHelper.performHapticClick(haptics)
            soundHapticsHelper.playSendSound(sound)
        }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            role = MessageRole.USER,
            content = content,
            imageUris = images,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            chatRepository.saveMessage(userMessage)
            _uiState.value = _uiState.value.copy(currentInputDraft = "")
            chatRepository.saveDraft(chatId, "")

            when (currentMode) {
                ChatMode.ASK_ANYTHING -> executeStandardChatStream(chatId, userMessage)
                ChatMode.BUILDER -> handleBuilderInteraction(chatId, content)
                ChatMode.MASTER_PROMPT -> handleMasterPromptInteraction(chatId, content)
            }
        }
    }

    private fun executeStandardChatStream(chatId: String, userMessage: ChatMessage) {
        streamingJob?.cancel()
        thinkingTimerJob?.cancel()

        _uiState.value = _uiState.value.copy(
            isStreaming = true,
            streamingText = "",
            streamingReasoning = "",
            errorMessage = null,
            liveSteps = listOf(
                WorkStep(title = "Connecting to AI provider…", status = StepStatus.RUNNING)
            )
        )

        AiGenerationService.startService(context, "Orvia AI Assistant")

        val startTime = System.currentTimeMillis()
        streamingJob = viewModelScope.launch {
            try {
                val globalSystemPrompt = preferencesManager.globalSystemPrompt.first()
                val memoryContext = memoryRepository.getMemoryContext()
                val systemPrompt = if (memoryContext.isNotBlank()) "$globalSystemPrompt\n\n$memoryContext" else globalSystemPrompt

                val currentHistory = _uiState.value.messages + userMessage
                val typingSpeed = preferencesManager.typingSpeedMs.first()

                providerManager.streamChatWithFallback(
                    messages = currentHistory,
                    systemPrompt = systemPrompt,
                    enableWebSearch = _uiState.value.webSearchEnabled,
                    enableThinking = _uiState.value.thinkingEnabled
                ).collect { chunk ->
                    if (chunk.reasoning.isNotBlank()) {
                        _uiState.value = _uiState.value.copy(
                            isThinking = true,
                            streamingReasoning = _uiState.value.streamingReasoning + chunk.reasoning
                        )
                    }

                    if (chunk.text.isNotBlank()) {
                        _uiState.value = _uiState.value.copy(
                            streamingText = _uiState.value.streamingText + chunk.text
                        )
                        if (typingSpeed > 0) delay(typingSpeed.toLong())
                    }
                }

                val duration = System.currentTimeMillis() - startTime
                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    role = MessageRole.ASSISTANT,
                    content = _uiState.value.streamingText,
                    rawReasoning = _uiState.value.streamingReasoning.ifBlank { null },
                    modelUsed = _uiState.value.activeModelName,
                    responseTimeMs = duration,
                    timestamp = System.currentTimeMillis()
                )

                chatRepository.saveMessage(aiMessage)
                chatRepository.generateTitleIfDefault(chatId, userMessage.content, aiMessage.content)

                // Sound & Notification
                val sound = preferencesManager.soundEnabled.first()
                soundHapticsHelper.playCompleteSound(sound)
                AiGenerationService.notifyCompletion(context, "Orvia", "Response complete.")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Generation error")
            } finally {
                _uiState.value = _uiState.value.copy(isStreaming = false, isThinking = false)
                AiGenerationService.stopService(context)
            }
        }
    }

    private suspend fun handleBuilderInteraction(chatId: String, content: String) {
        // If question interview is in progress
        if (_uiState.value.activeQuestion != null) {
            val q = _uiState.value.activeQuestion!!
            builderQaList.add(Pair(q.question, content))

            if (q.isComplete || builderQaList.size >= 15) {
                _uiState.value = _uiState.value.copy(activeQuestion = null)
                generateBuilderProject(chatId, "Project-${chatId.take(5)}")
            } else {
                val nextQ = builderRepository.getNextQuestion(
                    idea = builderQaList.firstOrNull()?.second ?: content,
                    previousAnswers = builderQaList,
                    currentQuestionNumber = builderQaList.size + 1
                )
                _uiState.value = _uiState.value.copy(activeQuestion = nextQ)
            }
        } else {
            // First prompt starts interview
            builderQaList.clear()
            val firstQ = builderRepository.getNextQuestion(
                idea = content,
                previousAnswers = emptyList(),
                currentQuestionNumber = 1
            )
            _uiState.value = _uiState.value.copy(activeQuestion = firstQ)
        }
    }

    private suspend fun generateBuilderProject(chatId: String, projectName: String) {
        _uiState.value = _uiState.value.copy(
            isStreaming = true,
            liveSteps = listOf(
                WorkStep(title = "Scoping project architecture…", status = StepStatus.RUNNING)
            )
        )

        try {
            val project = builderRepository.generateProject(
                chatId = chatId,
                projectName = projectName,
                idea = builderQaList.firstOrNull()?.second ?: projectName,
                qaTranscript = builderQaList
            ) { step ->
                _uiState.value = _uiState.value.copy(
                    liveSteps = _uiState.value.liveSteps + WorkStep(title = step, status = StepStatus.RUNNING)
                )
            }

            _uiState.value = _uiState.value.copy(activeProject = project)
            val summary = "Created project **${project.projectName}** with ${project.files.size} files."
            chatRepository.saveMessage(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    role = MessageRole.ASSISTANT,
                    content = summary
                )
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = e.message)
        } finally {
            _uiState.value = _uiState.value.copy(isStreaming = false)
        }
    }

    private suspend fun handleMasterPromptInteraction(chatId: String, content: String) {
        if (_uiState.value.activeMasterPrompt != null) {
            // In-place edit preview
            _uiState.value = _uiState.value.copy(isStreaming = true)
            try {
                val preview = masterPromptRepository.previewEdit(chatId, content)
                _uiState.value = _uiState.value.copy(pendingDiff = preview)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isStreaming = false)
            }
        } else if (_uiState.value.activeQuestion != null) {
            val q = _uiState.value.activeQuestion!!
            masterPromptQaList.add(Pair(q.question, content))

            if (q.isComplete || masterPromptQaList.size >= 15) {
                _uiState.value = _uiState.value.copy(activeQuestion = null)
                generateMasterPrompt(chatId)
            } else {
                val nextQ = masterPromptRepository.getNextQuestion(
                    idea = masterPromptQaList.firstOrNull()?.second ?: content,
                    qaHistory = masterPromptQaList,
                    currentNumber = masterPromptQaList.size + 1
                )
                _uiState.value = _uiState.value.copy(activeQuestion = nextQ)
            }
        } else {
            masterPromptQaList.clear()
            val firstQ = masterPromptRepository.getNextQuestion(
                idea = content,
                qaHistory = emptyList(),
                currentNumber = 1
            )
            _uiState.value = _uiState.value.copy(activeQuestion = firstQ)
        }
    }

    private suspend fun generateMasterPrompt(chatId: String) {
        _uiState.value = _uiState.value.copy(isStreaming = true)
        try {
            val promptVersion = masterPromptRepository.generateMasterPrompt(
                chatId = chatId,
                chatTitle = _uiState.value.currentChat?.title ?: "Master Prompt",
                idea = masterPromptQaList.firstOrNull()?.second ?: "Application",
                qaHistory = masterPromptQaList
            )
            _uiState.value = _uiState.value.copy(activeMasterPrompt = promptVersion)
            chatRepository.saveMessage(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    role = MessageRole.ASSISTANT,
                    content = "Generated **${promptVersion.fileName}** (Version 1).\n\n```markdown\n${promptVersion.content}\n```"
                )
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = e.message)
        } finally {
            _uiState.value = _uiState.value.copy(isStreaming = false)
        }
    }

    fun acceptPendingDiff() {
        val diff = _uiState.value.pendingDiff ?: return
        val chatId = _uiState.value.currentChat?.id ?: return
        viewModelScope.launch {
            val saved = masterPromptRepository.saveEditedVersion(
                chatId = chatId,
                updatedContent = diff.first,
                diffSummary = "+${diff.second.additions} / -${diff.second.deletions}"
            )
            _uiState.value = _uiState.value.copy(
                activeMasterPrompt = saved,
                pendingDiff = null
            )
            chatRepository.saveMessage(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    role = MessageRole.ASSISTANT,
                    content = "Updated **${saved.fileName}** to ${saved.versionName} (+${diff.second.additions} / -${diff.second.deletions})."
                )
            )
        }
    }

    fun discardPendingDiff() {
        _uiState.value = _uiState.value.copy(pendingDiff = null)
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        thinkingTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(isStreaming = false, isThinking = false)
        AiGenerationService.stopService(context)
    }

    fun retryLastMessage() {
        val lastUser = _uiState.value.messages.lastOrNull { it.role == MessageRole.USER }
        if (lastUser != null) {
            sendMessage(lastUser.content, lastUser.imageUris)
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
            if (_uiState.value.currentChat?.id == chatId) {
                startNewChat()
            }
        }
    }

    fun deleteMultipleChats(chatIds: List<String>) {
        viewModelScope.launch {
            chatRepository.deleteChats(chatIds)
            if (chatIds.contains(_uiState.value.currentChat?.id)) {
                startNewChat()
            }
        }
    }

    fun renameChat(chatId: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.updateChatTitle(chatId, newTitle)
        }
    }

    fun togglePin(chatId: String, isPinned: Boolean) {
        viewModelScope.launch {
            chatRepository.togglePin(chatId, isPinned)
        }
    }

    fun toggleArchive(chatId: String, isArchived: Boolean) {
        viewModelScope.launch {
            chatRepository.toggleArchive(chatId, isArchived)
        }
    }
}
