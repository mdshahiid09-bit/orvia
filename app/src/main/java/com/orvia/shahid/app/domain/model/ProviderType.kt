package com.orvia.shahid.app.domain.model

enum class ProviderType(val id: String, val displayName: String) {
    GEMINI("gemini", "Google Gemini"),
    GROQ("groq", "Groq"),
    OPENROUTER("openrouter", "OpenRouter"),
    CUSTOM("custom", "Custom Provider");

    companion object {
        fun fromId(id: String): ProviderType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CUSTOM
        }
    }
}
