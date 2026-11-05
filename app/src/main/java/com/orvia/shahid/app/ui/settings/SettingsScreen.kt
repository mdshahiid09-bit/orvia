package com.orvia.shahid.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orvia.shahid.app.R
import com.orvia.shahid.app.domain.model.ProviderType
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandCyan
import com.orvia.shahid.app.ui.theme.BrandPrimary
import com.orvia.shahid.app.ui.theme.DiffRemoveRed

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val memoryItems by viewModel.memoryItems.collectAsState()
    val customProviders by viewModel.customProviders.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_providers),
        stringResource(R.string.tab_chat),
        stringResource(R.string.tab_memory),
        stringResource(R.string.tab_github),
        stringResource(R.string.tab_appearance),
        stringResource(R.string.tab_sound),
        stringResource(R.string.tab_data),
        stringResource(R.string.tab_about)
    )

    var newMemoryInput by remember { mutableStateOf("") }
    var showConfirmClearChats by remember { mutableStateOf(false) }
    var showConfirmClearData by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0B1E))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = stringResource(R.string.cancel),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Tabs Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tabs.size) { index ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) BrandPrimary else Color(0xFF1E1638))
                                .border(1.dp, if (isSelected) BrandCyan else Color(0xFF33275A), RoundedCornerShape(20.dp))
                                .clickable { selectedTab = index }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = tabs[index],
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFFBEBAD0)
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0B0813)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> { // Providers
                    item {
                        ProviderConfigCard(
                            title = "Google Gemini",
                            key = uiState.geminiKey,
                            onKeyChange = { viewModel.updateGeminiKey(it) },
                            onTest = { viewModel.testProvider(ProviderType.GEMINI) }
                        )
                    }

                    item {
                        ProviderConfigCard(
                            title = "Groq",
                            key = uiState.groqKey,
                            onKeyChange = { viewModel.updateGroqKey(it) },
                            onTest = { viewModel.testProvider(ProviderType.GROQ) }
                        )
                    }

                    item {
                        ProviderConfigCard(
                            title = "OpenRouter",
                            key = uiState.openRouterKey,
                            onKeyChange = { viewModel.updateOpenRouterKey(it) },
                            onTest = { viewModel.testProvider(ProviderType.OPENROUTER) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.openrouter_free_only), color = Color.White, fontSize = 13.sp)
                                Switch(
                                    checked = uiState.openRouterFreeOnly,
                                    onCheckedChange = { viewModel.setOpenRouterFreeOnly(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BrandCyan)
                                )
                            }
                        }
                    }

                    if (!uiState.testStatus.isNullOrBlank()) {
                        item {
                            Text(
                                text = uiState.testStatus!!,
                                color = BrandCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                1 -> { // Chat Settings
                    item {
                        SettingCard(title = stringResource(R.string.default_model_label)) {
                            OutlinedTextField(
                                value = uiState.defaultModel,
                                onValueChange = { viewModel.setDefaultModel(it) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = inputColors()
                            )
                        }
                    }

                    item {
                        SettingCard(title = stringResource(R.string.system_instructions_label)) {
                            OutlinedTextField(
                                value = uiState.globalSystemPrompt,
                                onValueChange = { viewModel.setGlobalSystemPrompt(it) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                colors = inputColors()
                            )
                        }
                    }
                }

                2 -> { // Memory
                    item {
                        SettingCard(title = stringResource(R.string.memory_add)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newMemoryInput,
                                    onValueChange = { newMemoryInput = it },
                                    placeholder = { Text(stringResource(R.string.memory_hint), fontSize = 12.sp, color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    colors = inputColors()
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (newMemoryInput.isNotBlank()) {
                                            viewModel.addMemory(newMemoryInput.trim())
                                            newMemoryInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                                ) {
                                    Text(stringResource(R.string.confirm))
                                }
                            }
                        }
                    }

                    items(memoryItems) { memory ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF17112C), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF2C224B), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = memory.content, color = Color.White, fontSize = 13.5.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.deleteMemory(memory.id) }) {
                                Icon(painter = painterResource(id = R.drawable.ic_delete), contentDescription = null, tint = DiffRemoveRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                3 -> { // GitHub
                    item {
                        SettingCard(title = stringResource(R.string.github_token_label)) {
                            OutlinedTextField(
                                value = uiState.gitHubToken,
                                onValueChange = { viewModel.updateGitHubToken(it) },
                                placeholder = { Text("ghp_...", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = inputColors()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.testGitHubToken() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                            ) {
                                Text(stringResource(R.string.test_token))
                            }

                            if (!uiState.testStatus.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = uiState.testStatus!!, color = BrandCyan, fontSize = 13.sp)
                            }
                        }
                    }
                }

                4 -> { // Appearance
                    item {
                        SettingCard(title = stringResource(R.string.theme_mode_label)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("SYSTEM", "DARK", "LIGHT").forEach { mode ->
                                    val isSelected = uiState.themeMode.equals(mode, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) BrandPrimary else Color(0xFF1E1738))
                                            .clickable { viewModel.setThemeMode(mode) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = mode, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.animations_toggle), color = Color.White)
                                Switch(
                                    checked = uiState.animationsEnabled,
                                    onCheckedChange = { viewModel.setAnimationsEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BrandCyan)
                                )
                            }
                        }
                    }
                }

                5 -> { // Sound & Haptics
                    item {
                        SettingCard(title = stringResource(R.string.tab_sound)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.haptics_toggle), color = Color.White)
                                Switch(
                                    checked = uiState.hapticsEnabled,
                                    onCheckedChange = { viewModel.setHapticsEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BrandCyan)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.sound_toggle), color = Color.White)
                                Switch(
                                    checked = uiState.soundEnabled,
                                    onCheckedChange = { viewModel.setSoundEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary)
                                )
                            }
                        }
                    }
                }

                6 -> { // Data
                    item {
                        SettingCard(title = stringResource(R.string.tab_data)) {
                            OutlinedButton(
                                onClick = { showConfirmClearChats = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB4AB))
                            ) {
                                Text(stringResource(R.string.clear_all_chats))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { showConfirmClearData = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = DiffRemoveRed)
                            ) {
                                Text(stringResource(R.string.clear_all_data))
                            }
                        }
                    }
                }

                7 -> { // About
                    item {
                        SettingCard(title = stringResource(R.string.app_name)) {
                            Text("Version 1.0.0", color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Privacy-first autonomous AI Chat, Builder, and Master Prompt Generator.", color = Color.LightGray, fontSize = 12.5.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("https://github.com/mdshahiid09-bit/orvia", color = BrandCyan, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showConfirmClearChats) {
        AlertDialog(
            onDismissRequest = { showConfirmClearChats = false },
            title = { Text(stringResource(R.string.confirm_clear_title), color = Color.White) },
            text = { Text(stringResource(R.string.confirm_clear_message), color = Color.LightGray) },
            confirmButton = {
                Button(onClick = { viewModel.clearAllChats(); showConfirmClearChats = false }, colors = ButtonDefaults.buttonColors(containerColor = DiffRemoveRed)) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearChats = false }) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = Color(0xFF1E1738)
        )
    }

    if (showConfirmClearData) {
        AlertDialog(
            onDismissRequest = { showConfirmClearData = false },
            title = { Text(stringResource(R.string.confirm_clear_title), color = Color.White) },
            text = { Text(stringResource(R.string.confirm_clear_message), color = Color.LightGray) },
            confirmButton = {
                Button(onClick = { viewModel.clearAllData(); showConfirmClearData = false }, colors = ButtonDefaults.buttonColors(containerColor = DiffRemoveRed)) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearData = false }) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = Color(0xFF1E1738)
        )
    }
}

@Composable
private fun SettingCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151029))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandAccent)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ProviderConfigCard(
    title: String,
    key: String,
    onKeyChange: (String) -> Unit,
    onTest: () -> Unit,
    extraContent: @Composable () -> Unit = {}
) {
    SettingCard(title = title) {
        OutlinedTextField(
            value = key,
            onValueChange = onKeyChange,
            placeholder = { Text("API Key", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = inputColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onTest,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text(stringResource(R.string.test_connection))
            }
        }

        extraContent()
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandCyan,
    unfocusedBorderColor = Color(0xFF322756),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
