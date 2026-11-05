package com.orvia.shahid.app.di

import android.content.Context
import androidx.room.Room
import com.orvia.shahid.app.data.local.OrviaDatabase
import com.orvia.shahid.app.data.local.dao.BuilderProjectDao
import com.orvia.shahid.app.data.local.dao.ChatDao
import com.orvia.shahid.app.data.local.dao.CustomProviderDao
import com.orvia.shahid.app.data.local.dao.MasterPromptVersionDao
import com.orvia.shahid.app.data.local.dao.MemoryDao
import com.orvia.shahid.app.data.local.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OrviaDatabase {
        return Room.databaseBuilder(
            context,
            OrviaDatabase::class.java,
            "orvia_local_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideChatDao(db: OrviaDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideMessageDao(db: OrviaDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideMemoryDao(db: OrviaDatabase): MemoryDao = db.memoryDao()

    @Provides
    fun provideCustomProviderDao(db: OrviaDatabase): CustomProviderDao = db.customProviderDao()

    @Provides
    fun provideMasterPromptVersionDao(db: OrviaDatabase): MasterPromptVersionDao = db.masterPromptVersionDao()

    @Provides
    fun provideBuilderProjectDao(db: OrviaDatabase): BuilderProjectDao = db.builderProjectDao()
}
