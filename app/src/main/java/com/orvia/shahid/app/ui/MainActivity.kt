package com.orvia.shahid.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orvia.shahid.app.domain.model.ChatMode
import com.orvia.shahid.app.ui.chat.ChatScreen
import com.orvia.shahid.app.ui.chat.ChatViewModel
import com.orvia.shahid.app.ui.settings.SettingsScreen
import com.orvia.shahid.app.ui.settings.SettingsViewModel
import com.orvia.shahid.app.ui.theme.OrviaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle Widget launch
        if (intent?.action == ACTION_NEW_CHAT) {
            chatViewModel.startNewChat(ChatMode.ASK_ANYTHING)
        }

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsState()

            OrviaTheme(themeMode = settingsState.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0B0813)
                ) {
                    OrviaAppNavigation(
                        chatViewModel = chatViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    companion object {
        const val ACTION_NEW_CHAT = "com.orvia.shahid.app.ACTION_NEW_CHAT"
    }
}

@Composable
fun OrviaAppNavigation(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "chat") {
        composable("chat") {
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
