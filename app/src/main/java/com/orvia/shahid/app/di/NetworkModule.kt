package com.orvia.shahid.app.di

import com.google.gson.Gson
import com.orvia.shahid.app.data.remote.api.CustomOpenAIClient
import com.orvia.shahid.app.data.remote.api.GeminiClient
import com.orvia.shahid.app.data.remote.api.GitHubApiClient
import com.orvia.shahid.app.data.remote.api.GroqClient
import com.orvia.shahid.app.data.remote.api.OpenRouterClient
import com.orvia.shahid.app.data.remote.sse.SseStreamParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideSseStreamParser(gson: Gson): SseStreamParser = SseStreamParser(gson)

    @Provides
    @Singleton
    fun provideGeminiClient(okHttpClient: OkHttpClient, gson: Gson, sseParser: SseStreamParser): GeminiClient {
        return GeminiClient(okHttpClient, gson, sseParser)
    }

    @Provides
    @Singleton
    fun provideGroqClient(okHttpClient: OkHttpClient, gson: Gson, sseParser: SseStreamParser): GroqClient {
        return GroqClient(okHttpClient, gson, sseParser)
    }

    @Provides
    @Singleton
    fun provideOpenRouterClient(okHttpClient: OkHttpClient, gson: Gson, sseParser: SseStreamParser): OpenRouterClient {
        return OpenRouterClient(okHttpClient, gson, sseParser)
    }

    @Provides
    @Singleton
    fun provideCustomOpenAIClient(okHttpClient: OkHttpClient, gson: Gson, sseParser: SseStreamParser): CustomOpenAIClient {
        return CustomOpenAIClient(okHttpClient, gson, sseParser)
    }

    @Provides
    @Singleton
    fun provideGitHubApiClient(okHttpClient: OkHttpClient, gson: Gson): GitHubApiClient {
        return GitHubApiClient(okHttpClient, gson)
    }
}
