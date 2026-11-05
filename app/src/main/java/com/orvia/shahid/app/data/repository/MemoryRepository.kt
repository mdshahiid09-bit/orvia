package com.orvia.shahid.app.data.repository

import com.orvia.shahid.app.data.local.dao.MemoryDao
import com.orvia.shahid.app.data.local.entities.MemoryEntity
import com.orvia.shahid.app.domain.model.MemoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepository @Inject constructor(
    private val memoryDao: MemoryDao
) {

    fun getAllMemory(): Flow<List<MemoryItem>> = memoryDao.getAllMemory().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getMemoryContext(): String {
        val items = memoryDao.getAllMemoryDirect()
        if (items.isEmpty()) return ""
        return "Long-Term Memory / User Preferences:\n" + items.joinToString("\n") { "- ${it.content}" }
    }

    suspend fun addMemory(content: String): String {
        val id = UUID.randomUUID().toString()
        val entity = MemoryEntity(
            id = id,
            content = content.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        memoryDao.insertMemory(entity)
        return id
    }

    suspend fun updateMemory(id: String, content: String) {
        val entity = MemoryEntity(
            id = id,
            content = content.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        memoryDao.updateMemory(entity)
    }

    suspend fun deleteMemory(id: String) {
        memoryDao.deleteMemory(id)
    }

    suspend fun clearAllMemory() {
        memoryDao.clearAllMemory()
    }

    private fun MemoryEntity.toDomain() = MemoryItem(
        id = id,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
