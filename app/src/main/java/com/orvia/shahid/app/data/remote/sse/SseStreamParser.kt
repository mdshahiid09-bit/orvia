package com.orvia.shahid.app.data.remote.sse

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.orvia.shahid.app.data.remote.models.GeminiResponse
import com.orvia.shahid.app.data.remote.models.OpenAIChatResponse
import com.orvia.shahid.app.domain.model.StreamChunk

class SseStreamParser(private val gson: Gson = Gson()) {

    fun parseOpenAiChunk(line: String): StreamChunk? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("data:")) return null

        val dataContent = trimmed.substring(5).trim()
        if (dataContent == "[DONE]") {
            return StreamChunk(isFinished = true)
        }

        return try {
            val response = gson.fromJson(dataContent, OpenAIChatResponse::class.java)
            val choice = response.choices?.firstOrNull()
            val textDelta = choice?.delta?.content ?: ""
            val reasoningDelta = choice?.delta?.reasoningContent ?: ""
            val isDone = choice?.finishReason != null

            val inputTokens = response.usage?.promptTokens
            val outputTokens = response.usage?.completionTokens

            StreamChunk(
                text = textDelta,
                reasoning = reasoningDelta,
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                isFinished = isDone
            )
        } catch (e: Exception) {
            try {
                val jsonObject = gson.fromJson(dataContent, JsonObject::class.java)
                val choices = jsonObject.getAsJsonArray("choices")
                if (choices != null && choices.size() > 0) {
                    val first = choices[0].asJsonObject
                    val delta = first.getAsJsonObject("delta")
                    val content = delta?.get("content")?.asString ?: ""
                    val reasoning = delta?.get("reasoning_content")?.asString ?: ""
                    StreamChunk(text = content, reasoning = reasoning)
                } else null
            } catch (ex: Exception) {
                null
            }
        }
    }

    fun parseGeminiChunk(line: String): StreamChunk? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("data:")) return null

        val dataContent = trimmed.substring(5).trim()
        if (dataContent == "[DONE]" || dataContent.isEmpty()) {
            return StreamChunk(isFinished = true)
        }

        return try {
            val response = gson.fromJson(dataContent, GeminiResponse::class.java)
            val candidate = response.candidates?.firstOrNull()
            val textBuilder = StringBuilder()

            candidate?.content?.parts?.forEach { part ->
                part.text?.let { textBuilder.append(it) }
            }

            val isDone = candidate?.finishReason != null
            val inputTokens = response.usageMetadata?.promptTokenCount
            val outputTokens = response.usageMetadata?.candidatesTokenCount

            StreamChunk(
                text = textBuilder.toString(),
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                isFinished = isDone
            )
        } catch (e: Exception) {
            null
        }
    }
}
