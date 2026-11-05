package com.orvia.shahid.app

import com.orvia.shahid.app.data.remote.api.OpenRouterClient
import com.orvia.shahid.app.data.remote.models.OpenRouterModelDto
import com.orvia.shahid.app.data.remote.models.OpenRouterPricing
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OpenRouterFilterTest {

    private lateinit var client: OpenRouterClient

    @Before
    fun setUp() {
        client = OpenRouterClient(OkHttpClient())
    }

    @Test
    fun testFilterFreeModelsWithFreeSuffix() {
        val models = listOf(
            OpenRouterModelDto(id = "google/gemini-2.0-flash-exp:free", name = "Gemini Flash Free"),
            OpenRouterModelDto(id = "meta-llama/llama-3.3-70b-instruct:free", name = "Llama 3.3 Free"),
            OpenRouterModelDto(id = "openai/gpt-4o", name = "GPT-4o Paid")
        )

        val freeList = client.filterFreeModels(models)
        assertEquals(2, freeList.size)
        assertTrue(freeList.any { it.id == "google/gemini-2.0-flash-exp:free" })
        assertTrue(freeList.any { it.id == "meta-llama/llama-3.3-70b-instruct:free" })
        assertFalse(freeList.any { it.id == "openai/gpt-4o" })
    }

    @Test
    fun testFilterFreeModelsWithZeroPricing() {
        val models = listOf(
            OpenRouterModelDto(
                id = "mistralai/mistral-7b",
                name = "Mistral 7B",
                pricing = OpenRouterPricing(prompt = "0", completion = "0")
            ),
            OpenRouterModelDto(
                id = "anthropic/claude-3.5-sonnet",
                name = "Claude Sonnet",
                pricing = OpenRouterPricing(prompt = "0.000003", completion = "0.000015")
            )
        )

        val freeList = client.filterFreeModels(models)
        assertEquals(1, freeList.size)
        assertEquals("mistralai/mistral-7b", freeList[0].id)
    }
}
