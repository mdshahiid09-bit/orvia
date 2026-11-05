package com.orvia.shahid.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orvia.shahid.app.data.local.dao.CustomProviderDao
import com.orvia.shahid.app.data.local.datastore.PreferencesManager
import com.orvia.shahid.app.data.local.entities.CustomProviderEntity
import com.orvia.shahid.app.data.local.security.EncryptedKeyStorage
import com.orvia.shahid.app.data.repository.ChatRepository
import com.orvia.shahid.app.data.repository.GitHubRepository
import com.orvia.shahid.app.data.repository.MemoryRepository
import com.orvia.shahid.app.data.repository.ProviderManager
import com.orvia.shahid.app.domain.model.CustomProvider
import com.orvia.shahid.app.domain.model.MemoryItem
import com.orvia.shahid.app.domain.model.ProviderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SettingsUiState(
    val geminiKey: String = "",
    val groqKey: String = "",
    val openRouterKey: String = "",
    val gitHubToken: String = "",
    val themeMode: String = "SYSTEM",
    val animationsEnabled: Boolean = true,
    val fontSizeMultiplier: Float = 1.0f,
    val typingSpeedMs: Int = 15,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val voiceInputLang: String = "en-US",
    val globalSystemPrompt: String = "",
    val defaultModel: String = "gemini-1.5-flash",
    val openRouterFreeOnly: Boolean = true,
    val geminiThinkingLevel: String = "Medium",
    val fallbackOrder: List<String> = listOf("gemini", "openrouter", "groq"),
    val defaultRepoVisibility: String = "PUBLIC",
    val testStatus: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val keyStorage: EncryptedKeyStorage,
    private val preferencesManager: PreferencesManager,
    private val memoryRepository: MemoryRepository,
    private val customProviderDao: CustomProviderDao,
    private val providerManager: ProviderManager,
    private val gitHubRepository: GitHubRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val memoryItems: StateFlow<List<MemoryItem>> = memoryRepository.getAllMemory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customProviders: StateFlow<List<CustomProvider>> = customProviderDao.getAllProviders().map { list ->
        list.map { entity ->
            CustomProvider(
                id = entity.id,
                name = entity.name,
                baseUrl = entity.baseUrl,
                apiKey = keyStorage.decrypt(entity.encryptedApiKey),
                modelId = entity.modelId,
                authHeaderType = entity.authHeaderType,
                temperature = entity.temperature,
                maxTokens = entity.maxTokens,
                isEnabled = entity.isEnabled
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                geminiKey = keyStorage.getGeminiApiKey(),
                groqKey = keyStorage.getGroqApiKey(),
                openRouterKey = keyStorage.getOpenRouterApiKey(),
                gitHubToken = keyStorage.getGitHubToken()
            )

            preferencesManager.themeMode.collect { _uiState.value = _uiState.value.copy(themeMode = it) }
        }
        viewModelScope.launch {
            preferencesManager.animationsEnabled.collect { _uiState.value = _uiState.value.copy(animationsEnabled = it) }
        }
        viewModelScope.launch {
            preferencesManager.hapticsEnabled.collect { _uiState.value = _uiState.value.copy(hapticsEnabled = it) }
        }
        viewModelScope.launch {
            preferencesManager.soundEnabled.collect { _uiState.value = _uiState.value.copy(soundEnabled = it) }
        }
        viewModelScope.launch {
            preferencesManager.defaultModel.collect { _uiState.value = _uiState.value.copy(defaultModel = it) }
        }
        viewModelScope.launch {
            preferencesManager.globalSystemPrompt.collect { _uiState.value = _uiState.value.copy(globalSystemPrompt = it) }
        }
        viewModelScope.launch {
            preferencesManager.fallbackOrder.collect { _uiState.value = _uiState.value.copy(fallbackOrder = it) }
        }
        viewModelScope.launch {
            preferencesManager.openRouterFreeOnly.collect { _uiState.value = _uiState.value.copy(openRouterFreeOnly = it) }
        }
        viewModelScope.launch {
            preferencesManager.geminiThinkingLevel.collect { _uiState.value = _uiState.value.copy(geminiThinkingLevel = it) }
        }
    }

    // Key updates
    fun updateGeminiKey(key: String) {
        keyStorage.setGeminiApiKey(key)
        _uiState.value = _uiState.value.copy(geminiKey = key)
    }

    fun updateGroqKey(key: String) {
        keyStorage.setGroqApiKey(key)
        _uiState.value = _uiState.value.copy(groqKey = key)
    }

    fun updateOpenRouterKey(key: String) {
        keyStorage.setOpenRouterApiKey(key)
        _uiState.value = _uiState.value.copy(openRouterKey = key)
    }

    fun updateGitHubToken(token: String) {
        keyStorage.setGitHubToken(token)
        _uiState.value = _uiState.value.copy(gitHubToken = token)
    }

    // Provider connection tests
    fun testProvider(type: ProviderType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(testStatus = "Testing connection…")
            val result = providerManager.testProviderConnection(type)
            _uiState.value = _uiState.value.copy(
                testStatus = if (result.isSuccess) "Success (HTTP ${result.getOrNull()})" else "Failed: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun testGitHubToken() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(testStatus = "Verifying GitHub token…")
            val res = gitHubRepository.testToken()
            _uiState.value = _uiState.value.copy(
                testStatus = if (res.isSuccess) "Verified: ${res.getOrNull()?.login}" else "Error: ${res.exceptionOrNull()?.message}"
            )
        }
    }

    // Custom Providers
    fun saveCustomProvider(provider: CustomProvider) {
        viewModelScope.launch {
            val entity = CustomProviderEntity(
                id = if (provider.id.isBlank()) UUID.randomUUID().toString() else provider.id,
                name = provider.name,
                baseUrl = provider.baseUrl,
                encryptedApiKey = keyStorage.encrypt(provider.apiKey),
                modelId = provider.modelId,
                authHeaderType = provider.authHeaderType,
                temperature = provider.temperature,
                maxTokens = provider.maxTokens,
                isEnabled = provider.isEnabled,
                createdAt = System.currentTimeMillis()
            )
            customProviderDao.insertProvider(entity)
        }
    }

    fun deleteCustomProvider(id: String) {
        viewModelScope.launch { customProviderDao.deleteProvider(id) }
    }

    // Memory
    fun addMemory(content: String) {
        viewModelScope.launch { memoryRepository.addMemory(content) }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch { memoryRepository.deleteMemory(id) }
    }

    // Preferences Setters
    fun setThemeMode(mode: String) = viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    fun setAnimationsEnabled(enabled: Boolean) = viewModelScope.launch { preferencesManager.setAnimationsEnabled(enabled) }
    fun setHapticsEnabled(enabled: Boolean) = viewModelScope.launch { preferencesManager.setHapticsEnabled(enabled) }
    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch { preferencesManager.setSoundEnabled(enabled) }
    fun setDefaultModel(model: String) = viewModelScope.launch { preferencesManager.setDefaultModel(model) }
    fun setGlobalSystemPrompt(prompt: String) = viewModelScope.launch { preferencesManager.setGlobalSystemPrompt(prompt) }
    fun setOpenRouterFreeOnly(freeOnly: Boolean) = viewModelScope.launch { preferencesManager.setOpenRouterFreeOnly(freeOnly) }
    fun setGeminiThinkingLevel(level: String) = viewModelScope.launch { preferencesManager.setGeminiThinkingLevel(level) }

    fun updateFallbackOrder(order: List<String>) = viewModelScope.launch {
        preferencesManager.setFallbackOrder(order)
        _uiState.value = _uiState.value.copy(fallbackOrder = order)
    }

    // Data wipe
    fun clearAllChats() = viewModelScope.launch { chatRepository.clearAllChats() }
    fun clearAllData() = viewModelScope.launch {
        chatRepository.clearAllChats()
        memoryRepository.clearAllMemory()
        keyStorage.clearAllKeys()
        preferencesManager.clearAllPreferences()
    }
}
