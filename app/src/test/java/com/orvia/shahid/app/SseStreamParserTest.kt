package com.orvia.shahid.app

import com.google.gson.Gson
import com.orvia.shahid.app.data.remote.sse.SseStreamParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SseStreamParserTest {

    private lateinit var parser: SseStreamParser

    @Before
    fun setUp() {
        parser = SseStreamParser(Gson())
    }

    @Test
    fun testParseOpenAiContentChunk() {
        val sseLine = "data: {\"id\":\"chatcmpl-123\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hello world\"}}]}"
        val chunk = parser.parseOpenAiChunk(sseLine)

        assertNotNull(chunk)
        assertEquals("Hello world", chunk?.text)
        assertEquals("", chunk?.reasoning)
        assertEquals(false, chunk?.isFinished)
    }

    @Test
    fun testParseOpenAiReasoningChunk() {
        val sseLine = "data: {\"id\":\"chatcmpl-123\",\"choices\":[{\"index\":0,\"delta\":{\"reasoning_content\":\"Analyzing code...\"}}]}"
        val chunk = parser.parseOpenAiChunk(sseLine)

        assertNotNull(chunk)
        assertEquals("", chunk?.text)
        assertEquals("Analyzing code...", chunk?.reasoning)
    }

    @Test
    fun testParseOpenAiDoneChunk() {
        val sseLine = "data: [DONE]"
        val chunk = parser.parseOpenAiChunk(sseLine)

        assertNotNull(chunk)
        assertTrue(chunk?.isFinished == true)
    }

    @Test
    fun testParseGeminiChunk() {
        val sseLine = "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Namaste! Kaise ho?\"}]}}],\"usageMetadata\":{\"promptTokenCount\":12,\"candidatesTokenCount\":6}}"
        val chunk = parser.parseGeminiChunk(sseLine)

        assertNotNull(chunk)
        assertEquals("Namaste! Kaise ho?", chunk?.text)
        assertEquals(12, chunk?.inputTokens)
        assertEquals(6, chunk?.outputTokens)
    }

    @Test
    fun testNonDataLineReturnsNull() {
        val sseLine = "event: message"
        val chunk = parser.parseOpenAiChunk(sseLine)
        assertNull(chunk)
    }
}
