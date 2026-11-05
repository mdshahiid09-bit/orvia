package com.orvia.shahid.app

import com.google.gson.Gson
import com.orvia.shahid.app.data.remote.models.GeminiContent
import com.orvia.shahid.app.data.remote.models.GeminiGenerateContentRequest
import com.orvia.shahid.app.data.remote.models.GeminiPart
import com.orvia.shahid.app.data.remote.models.OpenAIChatMessage
import com.orvia.shahid.app.data.remote.models.OpenAIChatRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RequestBuilderTest {

    private val gson = Gson()

    @Test
    fun testGeminiRequestSerialization() {
        val request = GeminiGenerateContentRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Namaste"))
                )
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = "You are Orvia"))
            )
        )

        val json = gson.toJson(request)
        assertNotNull(json)
        assertTrue(json.contains("\"role\":\"user\""))
        assertTrue(json.contains("\"text\":\"Namaste\""))
        assertTrue(json.contains("\"text\":\"You are Orvia\""))
    }

    @Test
    fun testOpenAiChatRequestSerialization() {
        val request = OpenAIChatRequest(
            model = "llama-3.3-70b-versatile",
            messages = listOf(
                OpenAIChatMessage(role = "system", content = "Global Instructions"),
                OpenAIChatMessage(role = "user", content = "Hello Groq")
            ),
            temperature = 0.7f,
            stream = true
        )

        val json = gson.toJson(request)
        assertNotNull(json)
        assertTrue(json.contains("\"model\":\"llama-3.3-70b-versatile\""))
        assertTrue(json.contains("\"stream\":true"))
        assertTrue(json.contains("\"role\":\"system\""))
    }

    @Test
    fun testCustomProviderPayloadFormat() {
        val customMessages = listOf(
            OpenAIChatMessage(role = "user", content = "Test query")
        )
        val payload = OpenAIChatRequest(
            model = "custom-deepseek-r1",
            messages = customMessages,
            temperature = 0.5f,
            maxTokens = 2048,
            stream = true
        )

        val json = gson.toJson(payload)
        assertEquals(true, json.contains("\"max_tokens\":2048"))
        assertEquals(true, json.contains("\"model\":\"custom-deepseek-r1\""))
    }
}
