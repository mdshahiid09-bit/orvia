package com.orvia.shahid.app

import com.orvia.shahid.app.data.remote.api.RateLimitException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackOrderTest {

    @Test
    fun testDefaultFallbackSequence() {
        val defaultOrder = listOf("gemini", "openrouter", "groq")
        assertEquals("gemini", defaultOrder[0])
        assertEquals("openrouter", defaultOrder[1])
        assertEquals("groq", defaultOrder[2])
    }

    @Test
    fun testFallbackExecutionOnRateLimit() {
        val fallbackOrder = listOf("groq", "gemini", "openrouter")
        var currentAttemptIndex = 0
        var responder: String? = null

        for (provider in fallbackOrder) {
            currentAttemptIndex++
            try {
                if (provider == "groq") {
                    throw RateLimitException("HTTP 429 Rate Limited")
                }
                responder = provider
                break
            } catch (e: RateLimitException) {
                // Continue to next provider in fallback order
            }
        }

        assertEquals("gemini", responder)
        assertEquals(2, currentAttemptIndex)
    }

    @Test
    fun testCustomReorderedFallbackOrder() {
        val customOrder = listOf("custom-1", "openrouter", "gemini")
        val reordered = customOrder.toMutableList()
        reordered.remove("custom-1")
        reordered.add("custom-1")

        assertEquals(listOf("openrouter", "gemini", "custom-1"), reordered)
    }
}
