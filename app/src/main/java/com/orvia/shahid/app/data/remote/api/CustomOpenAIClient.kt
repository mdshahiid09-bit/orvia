package com.orvia.shahid.app.data.remote.api

import com.google.gson.Gson
import com.orvia.shahid.app.data.remote.models.OpenAIChatMessage
import com.orvia.shahid.app.data.remote.models.OpenAIChatRequest
import com.orvia.shahid.app.data.remote.sse.SseStreamParser
import com.orvia.shahid.app.domain.model.ChatMessage
import com.orvia.shahid.app.domain.model.CustomProvider
import com.orvia.shahid.app.domain.model.MessageRole
import com.orvia.shahid.app.domain.model.StreamChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader

class CustomOpenAIClient(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson = Gson(),
    private val sseParser: SseStreamParser = SseStreamParser(gson)
) {

    fun streamChat(
        provider: CustomProvider,
        messages: List<ChatMessage>,
        systemPrompt: String? = null
    ): Flow<StreamChunk> = flow {
        val endpoint = normalizeEndpoint(provider.baseUrl)

        val openAiMessages = mutableListOf<OpenAIChatMessage>()
        if (!systemPrompt.isNullOrBlank()) {
            openAiMessages.add(OpenAIChatMessage(role = "system", content = systemPrompt))
        }

        messages.forEach { msg ->
            val role = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "system"
            }
            openAiMessages.add(OpenAIChatMessage(role = role, content = msg.content))
        }

        val payload = OpenAIChatRequest(
            model = provider.modelId,
            messages = openAiMessages,
            temperature = provider.temperature,
            maxTokens = provider.maxTokens,
            stream = true
        )

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .header("Content-Type", "application/json")

        if (provider.apiKey.isNotBlank()) {
            val headerName = if (provider.authHeaderType.equals("Bearer", ignoreCase = true)) "Authorization" else provider.authHeaderType
            val headerValue = if (provider.authHeaderType.equals("Bearer", ignoreCase = true)) "Bearer ${provider.apiKey}" else provider.apiKey
            requestBuilder.header(headerName, headerValue)
        }

        provider.extraHeaders.forEach { (k, v) ->
            if (k.isNotBlank() && v.isNotBlank()) {
                requestBuilder.header(k, v)
            }
        }

        requestBuilder.post(gson.toJson(payload).toRequestBody("application/json".toMediaType()))

        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        if (!response.isSuccessful) {
            val error = response.body?.string() ?: "Unknown error"
            throw RuntimeException("HTTP ${response.code}: $error")
        }

        val reader = BufferedReader(InputStreamReader(response.body?.byteStream() ?: return@flow))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val currentLine = line ?: continue
            val chunk = sseParser.parseOpenAiChunk(currentLine)
            if (chunk != null) {
                emit(chunk.copy(model = provider.modelId, provider = provider.name))
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun testConnection(provider: CustomProvider): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val endpoint = normalizeEndpoint(provider.baseUrl)
            val testPayload = OpenAIChatRequest(
                model = provider.modelId,
                messages = listOf(OpenAIChatMessage(role = "user", content = "hi")),
                maxTokens = 5,
                stream = false
            )

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .header("Content-Type", "application/json")

            if (provider.apiKey.isNotBlank()) {
                val headerName = if (provider.authHeaderType.equals("Bearer", ignoreCase = true)) "Authorization" else provider.authHeaderType
                val headerValue = if (provider.authHeaderType.equals("Bearer", ignoreCase = true)) "Bearer ${provider.apiKey}" else provider.apiKey
                requestBuilder.header(headerName, headerValue)
            }

            provider.extraHeaders.forEach { (k, v) ->
                if (k.isNotBlank() && v.isNotBlank()) requestBuilder.header(k, v)
            }

            requestBuilder.post(gson.toJson(testPayload).toRequestBody("application/json".toMediaType()))

            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                Result.success(response.code)
            } else {
                val err = response.body?.string() ?: response.message
                Result.failure(RuntimeException("HTTP ${response.code}: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizeEndpoint(baseUrl: String): String {
        var base = baseUrl.trim()
        if (!base.endsWith("/")) base += "/"
        return if (base.endsWith("/chat/completions/")) {
            base.removeSuffix("/")
        } else if (base.endsWith("/v1/")) {
            "${base}chat/completions"
        } else {
            "${base}v1/chat/completions"
        }
    }
}
