package com.orvia.shahid.app.di

import android.content.Context
import com.orvia.shahid.app.data.local.datastore.PreferencesManager
import com.orvia.shahid.app.data.local.security.EncryptedKeyStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEncryptedKeyStorage(@ApplicationContext context: Context): EncryptedKeyStorage {
        return EncryptedKeyStorage(context)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}
