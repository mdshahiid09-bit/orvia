package com.orvia.shahid.app.ui.chat

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.orvia.shahid.app.R
import com.orvia.shahid.app.ui.builder.BuilderCodeViewerDialog
import com.orvia.shahid.app.ui.chat.components.ChatDrawerSheet
import com.orvia.shahid.app.ui.chat.components.ChatTopBar
import com.orvia.shahid.app.ui.chat.components.EmptyChatView
import com.orvia.shahid.app.ui.chat.components.InputBar
import com.orvia.shahid.app.ui.chat.components.MessageItem
import com.orvia.shahid.app.ui.common.AdaptiveQuestionBottomSheet
import com.orvia.shahid.app.ui.common.DiffViewerDialog
import com.orvia.shahid.app.ui.common.LiveWorkSummarySheet
import com.orvia.shahid.app.ui.common.StickyLiveSummaryBar
import com.orvia.shahid.app.ui.theme.BrandCyan
import com.orvia.shahid.app.ui.theme.BrandPrimary
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val allChats by viewModel.allChats.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var showLiveSummarySheet by remember { mutableStateOf(false) }
    var viewerFileContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(uiState.messages.size, uiState.streamingText.length) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    val showScrollToBottom by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible < total - 1
        }
    }

    // Voice recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.sendMessage(spokenText)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawerSheet(
                chats = allChats,
                currentChatId = uiState.currentChat?.id,
                onSelectChat = { id -> viewModel.selectChat(id) },
                onDeleteChat = { id -> viewModel.startNewChat() },
                onDeleteMultipleChats = { ids -> },
                onRenameChat = { id, title -> },
                onTogglePin = { id, pin -> },
                onToggleArchive = { id, arc -> },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    ChatTopBar(
                        currentMode = uiState.currentMode,
                        modelName = uiState.activeModelName,
                        onModeSelect = { mode -> viewModel.setChatMode(mode) },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNewChatClick = { viewModel.startNewChat() },
                        onSettingsClick = onNavigateToSettings
                    )

                    // Sticky Live Summary Bar (when steps exist)
                    if (uiState.liveSteps.isNotEmpty() || uiState.isStreaming) {
                        val currentStep = uiState.liveSteps.lastOrNull()?.title ?: "Processing request…"
                        val progress = "${uiState.liveSteps.size} steps"
                        StickyLiveSummaryBar(
                            currentStepText = currentStep,
                            stepProgress = progress,
                            onClick = { showLiveSummarySheet = true }
                        )
                    }
                }
            },
            bottomBar = {
                InputBar(
                    inputText = uiState.currentInputDraft,
                    onInputChange = { viewModel.onDraftChange(it) },
                    onSend = { viewModel.sendMessage(it) },
                    onStop = { viewModel.stopStreaming() },
                    onMicClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in Hindi, English, or Hinglish")
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {}
                    },
                    onAttachClick = {
                        // Launch file or image picker
                    },
                    isStreaming = uiState.isStreaming,
                    currentMode = uiState.currentMode,
                    modelName = uiState.activeModelName,
                    webSearchEnabled = uiState.webSearchEnabled,
                    onToggleWebSearch = { viewModel.toggleWebSearch() },
                    thinkingEnabled = uiState.thinkingEnabled,
                    onToggleThinking = { viewModel.toggleThinking() }
                )
            },
            containerColor = Color(0xFF0B0813)
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (uiState.messages.isEmpty() && !uiState.isStreaming) {
                    EmptyChatView(
                        currentMode = uiState.currentMode,
                        animationsEnabled = true,
                        onSuggestionClick = { viewModel.sendMessage(it) }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                    ) {
                        items(uiState.messages) { message ->
                            MessageItem(
                                message = message,
                                onEditAndResend = { text -> viewModel.onDraftChange(text) },
                                onRetry = { viewModel.retryLastMessage() }
                            )
                        }

                        // Live streaming reply placeholder
                        if (uiState.isStreaming && uiState.streamingText.isNotEmpty()) {
                            item {
                                MessageItem(
                                    message = com.orvia.shahid.app.domain.model.ChatMessage(
                                        id = "streaming",
                                        chatId = uiState.currentChat?.id ?: "",
                                        role = com.orvia.shahid.app.domain.model.MessageRole.ASSISTANT,
                                        content = uiState.streamingText + " ▌",
                                        rawReasoning = uiState.streamingReasoning.ifBlank { null }
                                    ),
                                    onEditAndResend = {},
                                    onRetry = {}
                                )
                            }
                        }
                    }
                }

                // Scroll to bottom button
                AnimatedVisibility(
                    visible = showScrollToBottom,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                if (uiState.messages.isNotEmpty()) {
                                    listState.animateScrollToItem(uiState.messages.lastIndex)
                                }
                            }
                        },
                        containerColor = BrandPrimary,
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Adaptive Question Bottom Sheet
    uiState.activeQuestion?.let { question ->
        AdaptiveQuestionBottomSheet(
            question = question,
            onAnswerSubmitted = { answer ->
                viewModel.sendMessage(answer)
            },
            onDismiss = {}
        )
    }

    // Live Work Summary Sheet
    if (showLiveSummarySheet) {
        LiveWorkSummarySheet(
            steps = uiState.liveSteps,
            isThinking = uiState.isThinking,
            thinkingElapsedSeconds = uiState.thinkingSeconds,
            onDismiss = { showLiveSummarySheet = false },
            onFileClick = { fileName ->
                val content = uiState.activeProject?.files?.get(fileName) ?: ""
                viewerFileContent = Pair(fileName, content)
            }
        )
    }

    // Master Prompt Diff Viewer Dialog
    uiState.pendingDiff?.let { diff ->
        DiffViewerDialog(
            diffResult = diff.second,
            onAccept = { viewModel.acceptPendingDiff() },
            onDiscard = { viewModel.discardPendingDiff() }
        )
    }

    // Code Viewer Dialog for Builder Files
    viewerFileContent?.let { (name, content) ->
        BuilderCodeViewerDialog(
            fileName = name,
            fileContent = content,
            onDismiss = { viewerFileContent = null }
        )
    }
}
