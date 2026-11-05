package com.orvia.shahid.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "orvia_preferences")

class PreferencesManager(private val context: Context) {

    private val gson = Gson()

    private val keyThemeMode = stringPreferencesKey("theme_mode")
    private val keyAnimations = booleanPreferencesKey("animations_enabled")
    private val keyFontSize = floatPreferencesKey("font_size_multiplier")
    private val keyTypingSpeed = intPreferencesKey("typing_speed_ms")
    private val keyHaptics = booleanPreferencesKey("haptics_enabled")
    private val keySound = booleanPreferencesKey("sound_enabled")
    private val keyVoiceInputLang = stringPreferencesKey("voice_input_lang")
    private val keySystemPrompt = stringPreferencesKey("global_system_prompt")
    private val keyDefaultModel = stringPreferencesKey("default_model")
    private val keyOpenRouterFreeOnly = booleanPreferencesKey("openrouter_free_only")
    private val keyOpenRouterFavorites = stringSetPreferencesKey("openrouter_favorites")
    private val keyGeminiThinkingLevel = stringPreferencesKey("gemini_thinking_level")
    private val keyGeminiSafety = stringPreferencesKey("gemini_safety")
    private val keyFallbackOrder = stringPreferencesKey("fallback_order_json")
    private val keyRepoVisibility = stringPreferencesKey("default_repo_visibility")

    val themeMode: Flow<String> = context.dataStore.data.map { it[keyThemeMode] ?: "SYSTEM" }
    val animationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[keyAnimations] ?: true }
    val fontSizeMultiplier: Flow<Float> = context.dataStore.data.map { it[keyFontSize] ?: 1.0f }
    val typingSpeedMs: Flow<Int> = context.dataStore.data.map { it[keyTypingSpeed] ?: 15 }
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { it[keyHaptics] ?: true }
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[keySound] ?: false }
    val voiceInputLang: Flow<String> = context.dataStore.data.map { it[keyVoiceInputLang] ?: "en-US" }
    val globalSystemPrompt: Flow<String> = context.dataStore.data.map {
        it[keySystemPrompt] ?: "You are Orvia, a knowledgeable and concise AI assistant. Respond helpfully in the user's preferred language."
    }
    val defaultModel: Flow<String> = context.dataStore.data.map { it[keyDefaultModel] ?: "gemini-1.5-flash" }
    val openRouterFreeOnly: Flow<Boolean> = context.dataStore.data.map { it[keyOpenRouterFreeOnly] ?: true }
    val openRouterFavorites: Flow<Set<String>> = context.dataStore.data.map { it[keyOpenRouterFavorites] ?: emptySet() }
    val geminiThinkingLevel: Flow<String> = context.dataStore.data.map { it[keyGeminiThinkingLevel] ?: "Medium" }
    val geminiSafetyThreshold: Flow<String> = context.dataStore.data.map { it[keyGeminiSafety] ?: "BLOCK_NONE" }
    val defaultRepoVisibility: Flow<String> = context.dataStore.data.map { it[keyRepoVisibility] ?: "PUBLIC" }

    val fallbackOrder: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[keyFallbackOrder]
        if (json.isNullOrEmpty()) {
            listOf("gemini", "openrouter", "groq")
        } else {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(json, type) ?: listOf("gemini", "openrouter", "groq")
            } catch (e: Exception) {
                listOf("gemini", "openrouter", "groq")
            }
        }
    }

    suspend fun setThemeMode(mode: String) = context.dataStore.edit { it[keyThemeMode] = mode }
    suspend fun setAnimationsEnabled(enabled: Boolean) = context.dataStore.edit { it[keyAnimations] = enabled }
    suspend fun setFontSizeMultiplier(multiplier: Float) = context.dataStore.edit { it[keyFontSize] = multiplier }
    suspend fun setTypingSpeedMs(speed: Int) = context.dataStore.edit { it[keyTypingSpeed] = speed }
    suspend fun setHapticsEnabled(enabled: Boolean) = context.dataStore.edit { it[keyHaptics] = enabled }
    suspend fun setSoundEnabled(enabled: Boolean) = context.dataStore.edit { it[keySound] = enabled }
    suspend fun setVoiceInputLang(lang: String) = context.dataStore.edit { it[keyVoiceInputLang] = lang }
    suspend fun setGlobalSystemPrompt(prompt: String) = context.dataStore.edit { it[keySystemPrompt] = prompt }
    suspend fun setDefaultModel(model: String) = context.dataStore.edit { it[keyDefaultModel] = model }
    suspend fun setOpenRouterFreeOnly(freeOnly: Boolean) = context.dataStore.edit { it[keyOpenRouterFreeOnly] = freeOnly }
    suspend fun toggleOpenRouterFavorite(modelId: String) = context.dataStore.edit { prefs ->
        val current = prefs[keyOpenRouterFavorites] ?: emptySet()
        prefs[keyOpenRouterFavorites] = if (current.contains(modelId)) current - modelId else current + modelId
    }
    suspend fun setGeminiThinkingLevel(level: String) = context.dataStore.edit { it[keyGeminiThinkingLevel] = level }
    suspend fun setGeminiSafetyThreshold(threshold: String) = context.dataStore.edit { it[keyGeminiSafety] = threshold }
    suspend fun setDefaultRepoVisibility(visibility: String) = context.dataStore.edit { it[keyRepoVisibility] = visibility }

    suspend fun setFallbackOrder(order: List<String>) = context.dataStore.edit {
        it[keyFallbackOrder] = gson.toJson(order)
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
