package com.orvia.shahid.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orvia.shahid.app.data.local.dao.ChatDao
import com.orvia.shahid.app.data.local.dao.MessageDao
import com.orvia.shahid.app.data.local.entities.ChatEntity
import com.orvia.shahid.app.data.local.entities.MessageEntity
import com.orvia.shahid.app.domain.model.ChatMessage
import com.orvia.shahid.app.domain.model.ChatMode
import com.orvia.shahid.app.domain.model.ChatSession
import com.orvia.shahid.app.domain.model.FileAttachment
import com.orvia.shahid.app.domain.model.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val providerManager: ProviderManager,
    private val gson: Gson = Gson()
) {

    fun getAllChats(): Flow<List<ChatSession>> = chatDao.getAllChats().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getChatById(id: String): Flow<ChatSession?> = chatDao.getChatById(id).map { it?.toDomain() }

    fun searchChatsByTitle(query: String): Flow<List<ChatSession>> =
        chatDao.searchChatsByTitle(query).map { entities -> entities.map { it.toDomain() } }

    fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>> =
        messageDao.getMessagesForChat(chatId).map { entities -> entities.map { it.toDomain(gson) } }

    suspend fun createNewChat(mode: ChatMode, title: String = "New Conversation"): String {
        val id = UUID.randomUUID().toString()
        val chatEntity = ChatEntity(
            id = id,
            title = title,
            mode = mode.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertChat(chatEntity)
        return id
    }

    suspend fun saveMessage(message: ChatMessage) {
        val entity = message.toEntity(gson)
        messageDao.insertMessage(entity)
        chatDao.updateChatTimestamp(message.chatId, System.currentTimeMillis())
    }

    suspend fun saveDraft(chatId: String, draft: String) {
        chatDao.updateDraft(chatId, draft)
    }

    suspend fun updateChatTitle(chatId: String, title: String) {
        chatDao.updateTitle(chatId, title, System.currentTimeMillis())
    }

    suspend fun togglePin(chatId: String, isPinned: Boolean) {
        chatDao.updatePinned(chatId, isPinned)
    }

    suspend fun toggleArchive(chatId: String, isArchived: Boolean) {
        chatDao.updateArchived(chatId, isArchived)
    }

    suspend fun deleteChat(chatId: String) {
        val chat = chatDao.getChatByIdDirect(chatId)
        if (chat != null) {
            chatDao.deleteChat(chat)
        }
    }

    suspend fun deleteChats(chatIds: List<String>) {
        chatDao.deleteChatsByIds(chatIds)
    }

    suspend fun clearAllChats() {
        chatDao.clearAllChats()
        messageDao.clearAllMessages()
    }

    /**
     * Automatically generates a concise 3-5 word title using the active AI provider.
     */
    suspend fun generateTitleIfDefault(chatId: String, firstUserMessage: String, firstAiResponse: String) {
        try {
            val prompt = "Create a short 3 to 5 word title for a conversation that starts with this user query: '$firstUserMessage'. Output only the title text, with no quotes or punctuation."
            val messages = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    role = MessageRole.USER,
                    content = prompt
                )
            )

            val titleBuilder = StringBuilder()
            providerManager.streamChatWithFallback(messages).collect { chunk ->
                titleBuilder.append(chunk.text)
            }

            val generatedTitle = titleBuilder.toString().trim().replace("\"", "").replace("\n", " ")
            if (generatedTitle.isNotBlank() && generatedTitle.length < 50) {
                chatDao.updateTitle(chatId, generatedTitle, System.currentTimeMillis())
            }
        } catch (e: Exception) {
            // Keep default title if title generation fails
        }
    }

    // Converters
    private fun ChatEntity.toDomain() = ChatSession(
        id = id,
        title = title,
        mode = runCatching { ChatMode.valueOf(mode) }.getOrDefault(ChatMode.ASK_ANYTHING),
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPinned = isPinned,
        isArchived = isArchived,
        draft = draft
    )

    private fun MessageEntity.toDomain(gson: Gson): ChatMessage {
        val imageList: List<String> = if (!imageUrisJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(imageUrisJson, type) ?: emptyList()
        } else emptyList()

        val fileList: List<FileAttachment> = if (!filesJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<FileAttachment>>() {}.type
            gson.fromJson(filesJson, type) ?: emptyList()
        } else emptyList()

        return ChatMessage(
            id = id,
            chatId = chatId,
            role = when (role.lowercase()) {
                "user" -> MessageRole.USER
                "assistant" -> MessageRole.ASSISTANT
                else -> MessageRole.SYSTEM
            },
            content = content,
            rawReasoning = rawReasoning,
            imageUris = imageList,
            fileAttachments = fileList,
            modelUsed = modelUsed,
            providerUsed = providerUsed,
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            responseTimeMs = responseTimeMs,
            timestamp = timestamp,
            isError = isError
        )
    }

    private fun ChatMessage.toEntity(gson: Gson) = MessageEntity(
        id = id,
        chatId = chatId,
        role = when (role) {
            MessageRole.USER -> "user"
            MessageRole.ASSISTANT -> "assistant"
            MessageRole.SYSTEM -> "system"
        },
        content = content,
        rawReasoning = rawReasoning,
        imageUrisJson = if (imageUris.isNotEmpty()) gson.toJson(imageUris) else null,
        filesJson = if (fileAttachments.isNotEmpty()) gson.toJson(fileAttachments) else null,
        modelUsed = modelUsed,
        providerUsed = providerUsed,
        inputTokens = inputTokens,
        outputTokens = outputTokens,
        responseTimeMs = responseTimeMs,
        timestamp = timestamp,
        isError = isError
    )
}
