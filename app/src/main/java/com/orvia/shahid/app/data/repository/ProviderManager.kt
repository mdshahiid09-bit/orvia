package com.orvia.shahid.app.data.repository

import com.orvia.shahid.app.data.local.dao.CustomProviderDao
import com.orvia.shahid.app.data.local.datastore.PreferencesManager
import com.orvia.shahid.app.data.local.security.EncryptedKeyStorage
import com.orvia.shahid.app.data.remote.api.CustomOpenAIClient
import com.orvia.shahid.app.data.remote.api.GeminiClient
import com.orvia.shahid.app.data.remote.api.GroqClient
import com.orvia.shahid.app.data.remote.api.OpenRouterClient
import com.orvia.shahid.app.domain.model.ChatMessage
import com.orvia.shahid.app.domain.model.CustomProvider
import com.orvia.shahid.app.domain.model.ProviderType
import com.orvia.shahid.app.domain.model.StreamChunk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderManager @Inject constructor(
    private val keyStorage: EncryptedKeyStorage,
    private val preferencesManager: PreferencesManager,
    private val customProviderDao: CustomProviderDao,
    private val geminiClient: GeminiClient,
    private val groqClient: GroqClient,
    private val openRouterClient: OpenRouterClient,
    private val customOpenAIClient: CustomOpenAIClient
) {

    fun streamChatWithFallback(
        messages: List<ChatMessage>,
        systemPrompt: String? = null,
        enableWebSearch: Boolean = false,
        enableThinking: Boolean = false
    ): Flow<StreamChunk> = flow {
        val fallbackOrder = preferencesManager.fallbackOrder.first()
        val defaultModel = preferencesManager.defaultModel.first()
        val thinkingLevel = if (enableThinking) preferencesManager.geminiThinkingLevel.first() else "Off"
        val safetyThreshold = preferencesManager.geminiSafetyThreshold.first()

        val customProviders = customProviderDao.getEnabledProviders().map { entity ->
            CustomProvider(
                id = entity.id,
                name = entity.name,
                baseUrl = entity.baseUrl,
                apiKey = keyStorage.decrypt(entity.encryptedApiKey),
                modelId = entity.modelId,
                authHeaderType = entity.authHeaderType,
                temperature = entity.temperature,
                maxTokens = entity.maxTokens
            )
        }

        var lastException: Throwable? = null

        for (providerId in fallbackOrder) {
            try {
                when (ProviderType.fromId(providerId)) {
                    ProviderType.GEMINI -> {
                        val apiKey = keyStorage.getGeminiApiKey()
                        if (apiKey.isNotBlank()) {
                            val model = if (defaultModel.contains("gemini", ignoreCase = true)) defaultModel else "gemini-1.5-flash"
                            geminiClient.streamChat(
                                apiKey = apiKey,
                                model = model,
                                messages = messages,
                                systemPrompt = systemPrompt,
                                enableWebSearch = enableWebSearch,
                                thinkingLevel = thinkingLevel,
                                safetyThreshold = safetyThreshold
                            ).collect { chunk -> emit(chunk) }
                            return@flow
                        }
                    }
                    ProviderType.OPENROUTER -> {
                        val apiKey = keyStorage.getOpenRouterApiKey()
                        if (apiKey.isNotBlank()) {
                            val model = if (defaultModel.contains("gemini", ignoreCase = true)) "google/gemini-2.0-flash-exp:free" else defaultModel
                            openRouterClient.streamChat(
                                apiKey = apiKey,
                                model = model,
                                messages = messages,
                                systemPrompt = systemPrompt
                            ).collect { chunk -> emit(chunk) }
                            return@flow
                        }
                    }
                    ProviderType.GROQ -> {
                        val apiKey = keyStorage.getGroqApiKey()
                        if (apiKey.isNotBlank()) {
                            val model = if (defaultModel.contains("llama", ignoreCase = true)) defaultModel else "llama-3.3-70b-versatile"
                            groqClient.streamChat(
                                apiKey = apiKey,
                                model = model,
                                messages = messages,
                                systemPrompt = systemPrompt
                            ).collect { chunk -> emit(chunk) }
                            return@flow
                        }
                    }
                    ProviderType.CUSTOM -> {
                        val custom = customProviders.firstOrNull { it.id == providerId }
                        if (custom != null && custom.apiKey.isNotBlank()) {
                            customOpenAIClient.streamChat(
                                provider = custom,
                                messages = messages,
                                systemPrompt = systemPrompt
                            ).collect { chunk -> emit(chunk) }
                            return@flow
                        }
                    }
                }
            } catch (e: Exception) {
                lastException = e
                delay(500)
            }
        }

        throw lastException ?: IllegalStateException(
            "No active AI provider could respond. Please configure an API key for Gemini, OpenRouter, Groq, or a Custom Provider in Settings."
        )
    }

    suspend fun testProviderConnection(providerType: ProviderType, customProvider: CustomProvider? = null): Result<Int> {
        return when (providerType) {
            ProviderType.GEMINI -> geminiClient.testConnection(keyStorage.getGeminiApiKey())
            ProviderType.GROQ -> groqClient.testConnection(keyStorage.getGroqApiKey())
            ProviderType.OPENROUTER -> openRouterClient.testConnection(keyStorage.getOpenRouterApiKey())
            ProviderType.CUSTOM -> {
                if (customProvider != null) {
                    customOpenAIClient.testConnection(customProvider)
                } else {
                    Result.failure(IllegalArgumentException("Custom provider details missing"))
                }
            }
        }
    }
}
