package com.orvia.shahid.app.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orvia.shahid.app.R
import com.orvia.shahid.app.domain.model.ChatMode
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandCyan
import com.orvia.shahid.app.ui.theme.BrandPrimary

@Composable
fun InputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onMicClick: () -> Unit,
    onAttachClick: () -> Unit,
    isStreaming: Boolean,
    currentMode: ChatMode,
    modelName: String,
    webSearchEnabled: Boolean,
    onToggleWebSearch: () -> Unit,
    thinkingEnabled: Boolean,
    onToggleThinking: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlusMenu by remember { mutableStateOf(false) }

    val hintText = when (currentMode) {
        ChatMode.ASK_ANYTHING -> stringResource(R.string.input_hint_ask)
        ChatMode.BUILDER -> stringResource(R.string.input_hint_builder)
        ChatMode.MASTER_PROMPT -> stringResource(R.string.input_hint_prompt)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0B1E))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Tag Chip: "Builder" mode or Model Name
        Row(
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentMode == ChatMode.BUILDER) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF281C48))
                        .border(0.5.dp, BrandCyan, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_builder),
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.builder_mode_chip),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Character count when input is long (> 1000 chars)
            if (inputText.length > 1000) {
                Text(
                    text = "${inputText.length} chars",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // Main Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF191330))
                .border(1.dp, Color(0xFF2F2454), RoundedCornerShape(24.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plus (+) Button with Popup Menu
            Box {
                IconButton(
                    onClick = { showPlusMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = null,
                        tint = BrandAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showPlusMenu,
                    onDismissRequest = { showPlusMenu = false },
                    modifier = Modifier.background(Color(0xFF1E1738))
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.attach_files), color = Color.White) },
                        leadingIcon = {
                            Icon(painter = painterResource(id = R.drawable.ic_attach), contentDescription = null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            showPlusMenu = false
                            onAttachClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.web_search_toggle), color = Color.White)
                                Switch(
                                    checked = webSearchEnabled,
                                    onCheckedChange = { onToggleWebSearch() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BrandCyan)
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(painter = painterResource(id = R.drawable.ic_web_search), contentDescription = null, tint = BrandAccent, modifier = Modifier.size(18.dp))
                        },
                        onClick = { onToggleWebSearch() }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.thinking_toggle), color = Color.White)
                                Switch(
                                    checked = thinkingEnabled,
                                    onCheckedChange = { onToggleThinking() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary)
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(painter = painterResource(id = R.drawable.ic_thinking), contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                        },
                        onClick = { onToggleThinking() }
                    )
                }
            }

            // Auto-expanding text field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 28.dp, max = 130.dp)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = hintText,
                        color = Color(0xFF6E6A82),
                        fontSize = 14.sp
                    )
                }

                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(BrandCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Enter && !keyEvent.isShiftPressed) {
                                if (inputText.isNotBlank() && !isStreaming) {
                                    onSend(inputText)
                                    true
                                } else false
                            } else false
                        }
                )
            }

            // Clear Button (when text exists)
            if (inputText.isNotEmpty()) {
                IconButton(
                    onClick = { onInputChange("") },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clear),
                        contentDescription = stringResource(R.string.clear_text),
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Right Action: Stop while streaming, Send when non-empty, Mic when empty
            if (isStreaming) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFF8B2635), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stop),
                        contentDescription = stringResource(R.string.action_stop),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else if (inputText.isNotBlank()) {
                IconButton(
                    onClick = { onSend(inputText) },
                    modifier = Modifier
                        .size(34.dp)
                        .background(BrandPrimary, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_send),
                        contentDescription = stringResource(R.string.action_send),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = stringResource(R.string.voice_input),
                        tint = BrandAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
