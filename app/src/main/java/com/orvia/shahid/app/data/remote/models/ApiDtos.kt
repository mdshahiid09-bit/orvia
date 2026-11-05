package com.orvia.shahid.app.data.remote.models

import com.google.gson.annotations.SerializedName

// Gemini Models
data class GeminiGenerateContentRequest(
    @SerializedName("contents") val contents: List<GeminiContent>,
    @SerializedName("system_instruction") val systemInstruction: GeminiContent? = null,
    @SerializedName("tools") val tools: List<GeminiTool>? = null,
    @SerializedName("generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @SerializedName("safetySettings") val safetySettings: List<GeminiSafetySetting>? = null
)

data class GeminiContent(
    @SerializedName("role") val role: String? = null,
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text") val text: String? = null,
    @SerializedName("inline_data") val inlineData: GeminiInlineData? = null
)

data class GeminiInlineData(
    @SerializedName("mime_type") val mimeType: String,
    @SerializedName("data") val base64Data: String
)

data class GeminiTool(
    @SerializedName("google_search") val googleSearch: Map<String, Any>? = null
)

data class GeminiGenerationConfig(
    @SerializedName("temperature") val temperature: Float? = null,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int? = null,
    @SerializedName("topP") val topP: Float? = null,
    @SerializedName("thinkingConfig") val thinkingConfig: Map<String, Any>? = null
)

data class GeminiSafetySetting(
    @SerializedName("category") val category: String,
    @SerializedName("threshold") val threshold: String
)

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>? = null,
    @SerializedName("usageMetadata") val usageMetadata: GeminiUsageMetadata? = null
)

data class GeminiCandidate(
    @SerializedName("content") val content: GeminiContent? = null,
    @SerializedName("finishReason") val finishReason: String? = null
)

data class GeminiUsageMetadata(
    @SerializedName("promptTokenCount") val promptTokenCount: Int = 0,
    @SerializedName("candidatesTokenCount") val candidatesTokenCount: Int = 0,
    @SerializedName("totalTokenCount") val totalTokenCount: Int = 0
)

data class GeminiModelListResponse(
    @SerializedName("models") val models: List<GeminiModelDto>? = null
)

data class GeminiModelDto(
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("description") val description: String? = null
)

// OpenAI-Compatible (Groq, OpenRouter, Custom Providers)
data class OpenAIChatRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<OpenAIChatMessage>,
    @SerializedName("temperature") val temperature: Float? = null,
    @SerializedName("max_tokens") val maxTokens: Int? = null,
    @SerializedName("stream") val stream: Boolean = false
)

data class OpenAIChatMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: Any
)

data class OpenAIChatResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("choices") val choices: List<OpenAIChoice>? = null,
    @SerializedName("usage") val usage: OpenAIUsage? = null
)

data class OpenAIChoice(
    @SerializedName("index") val index: Int = 0,
    @SerializedName("delta") val delta: OpenAIDelta? = null,
    @SerializedName("message") val message: OpenAIChatMessageDto? = null,
    @SerializedName("finish_reason") val finishReason: String? = null
)

data class OpenAIChatMessageDto(
    @SerializedName("role") val role: String? = null,
    @SerializedName("content") val content: String? = null
)

data class OpenAIDelta(
    @SerializedName("role") val role: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("reasoning_content") val reasoningContent: String? = null
)

data class OpenAIUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int = 0,
    @SerializedName("completion_tokens") val completionTokens: Int = 0,
    @SerializedName("total_tokens") val totalTokens: Int = 0
)

data class OpenAIModelListResponse(
    @SerializedName("data") val data: List<OpenAIModelDto>? = null
)

data class OpenAIModelDto(
    @SerializedName("id") val id: String,
    @SerializedName("owned_by") val ownedBy: String? = null
)

// OpenRouter Models with Pricing
data class OpenRouterModelListResponse(
    @SerializedName("data") val data: List<OpenRouterModelDto>? = null
)

data class OpenRouterModelDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("pricing") val pricing: OpenRouterPricing? = null
)

data class OpenRouterPricing(
    @SerializedName("prompt") val prompt: String? = null,
    @SerializedName("completion") val completion: String? = null
) {
    val isFree: Boolean
        get() = (prompt == "0" || prompt == "0.0" || prompt?.startsWith("0.000000") == true) &&
                (completion == "0" || completion == "0.0" || completion?.startsWith("0.000000") == true)
}

// GitHub API Models
data class GitHubUserDto(
    @SerializedName("login") val login: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class GitHubCreateRepoRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("private") val isPrivate: Boolean,
    @SerializedName("auto_init") val autoInit: Boolean = true
)

data class GitHubRepoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("html_url") val htmlUrl: String
)

data class GitHubCreateFileRequest(
    @SerializedName("message") val message: String,
    @SerializedName("content") val base64Content: String,
    @SerializedName("branch") val branch: String = "main",
    @SerializedName("sha") val sha: String? = null
)
