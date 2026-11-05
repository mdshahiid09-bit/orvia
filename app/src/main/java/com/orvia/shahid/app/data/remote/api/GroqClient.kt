package com.orvia.shahid.app.data.remote.api

import com.google.gson.Gson
import com.orvia.shahid.app.data.remote.models.OpenAIChatMessage
import com.orvia.shahid.app.data.remote.models.OpenAIChatRequest
import com.orvia.shahid.app.data.remote.models.OpenAIModelListResponse
import com.orvia.shahid.app.data.remote.sse.SseStreamParser
import com.orvia.shahid.app.domain.model.ChatMessage
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

class RateLimitException(message: String) : RuntimeException(message)

class GroqClient(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson = Gson(),
    private val sseParser: SseStreamParser = SseStreamParser(gson)
) {

    fun streamChat(
        apiKey: String,
        model: String,
        messages: List<ChatMessage>,
        systemPrompt: String? = null
    ): Flow<StreamChunk> = flow {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("Groq API key is missing. Please add it in Settings.")
        }

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
            model = model,
            messages = openAiMessages,
            temperature = 0.7f,
            stream = true
        )

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(gson.toJson(payload).toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (response.code == 429) {
            throw RateLimitException("Groq Rate Limit Exceeded (HTTP 429). Switching to next provider.")
        }
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
                emit(chunk.copy(model = model, provider = "Groq"))
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun fetchModels(apiKey: String): List<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext emptyList()
        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/models")
            .header("Authorization", "Bearer $apiKey")
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("HTTP ${response.code}: ${response.body?.string()}")
        }

        val json = response.body?.string() ?: return@withContext emptyList()
        val list = gson.fromJson(json, OpenAIModelListResponse::class.java)
        list.data?.map { it.id } ?: listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant")
    }

    suspend fun testConnection(apiKey: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.groq.com/openai/v1/models")
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(response.code)
            } else {
                Result.failure(RuntimeException("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
