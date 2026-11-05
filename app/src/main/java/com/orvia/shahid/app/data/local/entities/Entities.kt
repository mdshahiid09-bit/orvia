package com.orvia.shahid.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chats",
    indices = [Index(value = ["updatedAt"]), Index(value = ["isPinned"])]
)
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val mode: String, // "ASK_ANYTHING", "BUILDER", "MASTER_PROMPT"
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val draft: String = ""
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatId"]), Index(value = ["timestamp"])]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val rawReasoning: String? = null,
    val imageUrisJson: String? = null,
    val filesJson: String? = null,
    val modelUsed: String? = null,
    val providerUsed: String? = null,
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
    val responseTimeMs: Long? = null,
    val timestamp: Long,
    val isError: Boolean = false
)

@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "custom_providers")
data class CustomProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val baseUrl: String,
    val encryptedApiKey: String,
    val modelId: String,
    val authHeaderType: String = "Bearer",
    val extraHeadersJson: String = "{}",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val isEnabled: Boolean = true,
    val createdAt: Long
)

@Entity(
    tableName = "master_prompt_versions",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatId"]), Index(value = ["timestamp"])]
)
data class MasterPromptVersionEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val versionNumber: Int,
    val versionName: String,
    val fileName: String,
    val content: String,
    val patchDiff: String? = null,
    val timestamp: Long
)

@Entity(
    tableName = "builder_projects",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatId"])]
)
data class BuilderProjectEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val projectName: String,
    val projectType: String,
    val filesJson: String,
    val version: Int,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "builder_versions",
    foreignKeys = [
        ForeignKey(
            entity = BuilderProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class BuilderVersionEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val versionNumber: Int,
    val patchSummary: String,
    val diffJson: String,
    val timestamp: Long
)
