package com.orvia.shahid.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.orvia.shahid.app.data.local.entities.BuilderProjectEntity
import com.orvia.shahid.app.data.local.entities.BuilderVersionEntity
import com.orvia.shahid.app.data.local.entities.ChatEntity
import com.orvia.shahid.app.data.local.entities.CustomProviderEntity
import com.orvia.shahid.app.data.local.entities.MasterPromptVersionEntity
import com.orvia.shahid.app.data.local.entities.MemoryEntity
import com.orvia.shahid.app.data.local.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :id")
    fun getChatById(id: String): Flow<ChatEntity?>

    @Query("SELECT * FROM chats WHERE id = :id")
    suspend fun getChatByIdDirect(id: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchChatsByTitle(query: String): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Delete
    suspend fun deleteChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id IN (:ids)")
    suspend fun deleteChatsByIds(ids: List<String>)

    @Query("DELETE FROM chats")
    suspend fun clearAllChats()

    @Query("UPDATE chats SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTitle(id: String, title: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chats SET draft = :draft WHERE id = :id")
    suspend fun updateDraft(id: String, draft: String)

    @Query("UPDATE chats SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)

    @Query("UPDATE chats SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateArchived(id: String, isArchived: Boolean)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessagesForChatDirect(chatId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory ORDER BY createdAt DESC")
    fun getAllMemory(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memory ORDER BY createdAt DESC")
    suspend fun getAllMemoryDirect(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Query("DELETE FROM memory WHERE id = :id")
    suspend fun deleteMemory(id: String)

    @Query("DELETE FROM memory")
    suspend fun clearAllMemory()
}

@Dao
interface CustomProviderDao {
    @Query("SELECT * FROM custom_providers ORDER BY createdAt DESC")
    fun getAllProviders(): Flow<List<CustomProviderEntity>>

    @Query("SELECT * FROM custom_providers WHERE isEnabled = 1")
    suspend fun getEnabledProviders(): List<CustomProviderEntity>

    @Query("SELECT * FROM custom_providers WHERE id = :id")
    suspend fun getProviderById(id: String): CustomProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: CustomProviderEntity)

    @Update
    suspend fun updateProvider(provider: CustomProviderEntity)

    @Query("DELETE FROM custom_providers WHERE id = :id")
    suspend fun deleteProvider(id: String)
}

@Dao
interface MasterPromptVersionDao {
    @Query("SELECT * FROM master_prompt_versions WHERE chatId = :chatId ORDER BY versionNumber DESC")
    fun getVersionsForChat(chatId: String): Flow<List<MasterPromptVersionEntity>>

    @Query("SELECT * FROM master_prompt_versions WHERE chatId = :chatId ORDER BY versionNumber DESC LIMIT 1")
    suspend fun getLatestVersionForChat(chatId: String): MasterPromptVersionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: MasterPromptVersionEntity)

    @Query("DELETE FROM master_prompt_versions WHERE chatId = :chatId")
    suspend fun deleteVersionsForChat(chatId: String)
}

@Dao
interface BuilderProjectDao {
    @Query("SELECT * FROM builder_projects WHERE chatId = :chatId")
    fun getProjectForChat(chatId: String): Flow<BuilderProjectEntity?>

    @Query("SELECT * FROM builder_projects WHERE chatId = :chatId")
    suspend fun getProjectForChatDirect(chatId: String): BuilderProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProject(project: BuilderProjectEntity)

    @Query("SELECT * FROM builder_versions WHERE projectId = :projectId ORDER BY versionNumber DESC")
    fun getVersionsForProject(projectId: String): Flow<List<BuilderVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: BuilderVersionEntity)
}
