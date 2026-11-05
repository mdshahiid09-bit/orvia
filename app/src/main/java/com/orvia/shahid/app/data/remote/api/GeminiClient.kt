package com.orvia.shahid.app.data.remote.api

import com.google.gson.Gson
import com.orvia.shahid.app.data.remote.models.GeminiContent
import com.orvia.shahid.app.data.remote.models.GeminiGenerateContentRequest
import com.orvia.shahid.app.data.remote.models.GeminiGenerationConfig
import com.orvia.shahid.app.data.remote.models.GeminiModelListResponse
import com.orvia.shahid.app.data.remote.models.GeminiPart
import com.orvia.shahid.app.data.remote.models.GeminiSafetySetting
import com.orvia.shahid.app.data.remote.models.GeminiTool
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

class GeminiClient(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson = Gson(),
    private val sseParser: SseStreamParser = SseStreamParser(gson)
) {

    fun streamChat(
        apiKey: String,
        model: String,
        messages: List<ChatMessage>,
        systemPrompt: String? = null,
        enableWebSearch: Boolean = false,
        thinkingLevel: String = "Medium",
        safetyThreshold: String = "BLOCK_NONE"
    ): Flow<StreamChunk> = flow {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("Gemini API key is missing. Please add it in Settings.")
        }

        val formattedModel = if (model.startsWith("models/")) model else "models/$model"
        val url = "https://generativelanguage.googleapis.com/v1beta/$formattedModel:streamGenerateContent?alt=sse&key=$apiKey"

        val contents = messages.map { msg ->
            val role = if (msg.role == MessageRole.USER) "user" else "model"
            GeminiContent(
                role = role,
                parts = listOf(GeminiPart(text = msg.content))
            )
        }

        val systemInstruction = systemPrompt?.let {
            GeminiContent(parts = listOf(GeminiPart(text = it)))
        }

        val tools = if (enableWebSearch) {
            listOf(GeminiTool(googleSearch = emptyMap()))
        } else null

        val generationConfig = GeminiGenerationConfig(
            temperature = 0.7f,
            maxOutputTokens = 8192
        )

        val safetyCategories = listOf(
            "HARM_CATEGORY_HARASSMENT",
            "HARM_CATEGORY_HATE_SPEECH",
            "HARM_CATEGORY_SEXUALLY_EXPLICIT",
            "HARM_CATEGORY_DANGEROUS_CONTENT"
        )
        val safetySettings = safetyCategories.map {
            GeminiSafetySetting(category = it, threshold = safetyThreshold)
        }

        val requestPayload = GeminiGenerateContentRequest(
            contents = contents,
            systemInstruction = systemInstruction,
            tools = tools,
            generationConfig = generationConfig,
            safetySettings = safetySettings
        )

        val jsonBody = gson.toJson(requestPayload)
        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            throw RuntimeException("HTTP ${response.code}: $errorBody")
        }

        val reader = BufferedReader(InputStreamReader(response.body?.byteStream() ?: return@flow))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val currentLine = line ?: continue
            val chunk = sseParser.parseGeminiChunk(currentLine)
            if (chunk != null) {
                emit(chunk.copy(model = model, provider = "Google Gemini"))
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun fetchModels(apiKey: String): List<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext emptyList()
        val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
        val request = Request.Builder().url(url).get().build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("HTTP ${response.code}: ${response.body?.string()}")
        }

        val json = response.body?.string() ?: return@withContext emptyList()
        val modelList = gson.fromJson(json, GeminiModelListResponse::class.java)
        modelList.models?.map { it.name.removePrefix("models/") } ?: listOf("gemini-1.5-flash", "gemini-1.5-pro")
    }

    suspend fun testConnection(apiKey: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
            val request = Request.Builder().url(url).get().build()
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
