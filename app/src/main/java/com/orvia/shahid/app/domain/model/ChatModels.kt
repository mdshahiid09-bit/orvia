package com.orvia.shahid.app.domain.model

data class ChatMessage(
    val id: String,
    val chatId: String,
    val role: MessageRole,
    val content: String,
    val rawReasoning: String? = null,
    val imageUris: List<String> = emptyList(),
    val fileAttachments: List<FileAttachment> = emptyList(),
    val modelUsed: String? = null,
    val providerUsed: String? = null,
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
    val responseTimeMs: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val isStreaming: Boolean = false
)

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

data class FileAttachment(
    val name: String,
    val path: String,
    val mimeType: String,
    val sizeBytes: Long
)

data class ChatSession(
    val id: String,
    val title: String,
    val mode: ChatMode,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val draft: String = ""
)

data class MemoryItem(
    val id: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CustomProvider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val modelId: String,
    val authHeaderType: String = "Bearer",
    val extraHeaders: Map<String, String> = emptyMap(),
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class BuilderProject(
    val id: String,
    val chatId: String,
    val projectName: String,
    val projectType: String, // "ANDROID", "WEB", "BACKEND"
    val files: Map<String, String> = emptyMap(), // relative path -> content
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class MasterPromptVersion(
    val id: String,
    val chatId: String,
    val versionNumber: Int,
    val versionName: String,
    val fileName: String,
    val content: String,
    val patchDiff: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class StreamChunk(
    val text: String = "",
    val reasoning: String = "",
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
    val isFinished: Boolean = false,
    val model: String? = null,
    val provider: String? = null
)
