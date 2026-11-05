package com.orvia.shahid.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.orvia.shahid.app.data.local.dao.BuilderProjectDao
import com.orvia.shahid.app.data.local.dao.ChatDao
import com.orvia.shahid.app.data.local.dao.CustomProviderDao
import com.orvia.shahid.app.data.local.dao.MasterPromptVersionDao
import com.orvia.shahid.app.data.local.dao.MemoryDao
import com.orvia.shahid.app.data.local.dao.MessageDao
import com.orvia.shahid.app.data.local.entities.BuilderProjectEntity
import com.orvia.shahid.app.data.local.entities.BuilderVersionEntity
import com.orvia.shahid.app.data.local.entities.ChatEntity
import com.orvia.shahid.app.data.local.entities.CustomProviderEntity
import com.orvia.shahid.app.data.local.entities.MasterPromptVersionEntity
import com.orvia.shahid.app.data.local.entities.MemoryEntity
import com.orvia.shahid.app.data.local.entities.MessageEntity

@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        CustomProviderEntity::class,
        MasterPromptVersionEntity::class,
        BuilderProjectEntity::class,
        BuilderVersionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OrviaDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun customProviderDao(): CustomProviderDao
    abstract fun masterPromptVersionDao(): MasterPromptVersionDao
    abstract fun builderProjectDao(): BuilderProjectDao
}
