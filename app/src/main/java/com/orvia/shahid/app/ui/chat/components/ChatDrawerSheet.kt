package com.orvia.shahid.app.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.orvia.shahid.app.domain.model.ChatSession
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandCyan
import com.orvia.shahid.app.ui.theme.BrandPrimary
import com.orvia.shahid.app.ui.theme.DiffRemoveRed

@Composable
fun ChatDrawerSheet(
    chats: List<ChatSession>,
    currentChatId: String?,
    onSelectChat: (String) -> Unit,
    onDeleteChat: (String) -> Unit,
    onDeleteMultipleChats: (List<String>) -> Unit,
    onRenameChat: (String, String) -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onToggleArchive: (String, Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedChatIds = remember { mutableStateListOf<String>() }
    var renameDialogChat by remember { mutableStateOf<ChatSession?>(null) }
    var newTitleInput by remember { mutableStateOf("") }

    val filteredChats = chats.filter {
        searchQuery.isBlank() || it.title.contains(searchQuery.trim(), ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(Color(0xFF100C22))
            .padding(16.dp)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.drawer_title_chats),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row {
                if (isMultiSelectMode) {
                    IconButton(
                        onClick = {
                            if (selectedChatIds.isNotEmpty()) {
                                onDeleteMultipleChats(selectedChatIds.toList())
                                selectedChatIds.clear()
                                isMultiSelectMode = false
                            }
                        }
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_delete), contentDescription = null, tint = DiffRemoveRed)
                    }
                }

                IconButton(
                    onClick = {
                        isMultiSelectMode = !isMultiSelectMode
                        selectedChatIds.clear()
                    }
                ) {
                    Icon(
                        painter = painterResource(id = if (isMultiSelectMode) R.drawable.ic_close else R.drawable.ic_check),
                        contentDescription = null,
                        tint = BrandCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.search_chats_hint), fontSize = 13.sp, color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandCyan,
                unfocusedBorderColor = Color(0xFF2C224B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Chat List
        if (filteredChats.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_chats_found), color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredChats) { chat ->
                    val isSelected = chat.id == currentChatId
                    val isChecked = selectedChatIds.contains(chat.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF241A45) else Color(0xFF15102C))
                            .border(0.5.dp, if (isSelected) BrandPrimary else Color(0xFF281E48), RoundedCornerShape(10.dp))
                            .clickable {
                                if (isMultiSelectMode) {
                                    if (isChecked) selectedChatIds.remove(chat.id) else selectedChatIds.add(chat.id)
                                } else {
                                    onSelectChat(chat.id)
                                    onClose()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isMultiSelectMode) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedChatIds.add(chat.id) else selectedChatIds.remove(chat.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = BrandCyan)
                            )
                        }

                        if (chat.isPinned) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_pin),
                                contentDescription = null,
                                tint = BrandCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chat.title,
                                color = if (isSelected) Color.White else Color(0xFFD6D4E4),
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                            Text(
                                text = chat.mode.name.replace("_", " ").lowercase(),
                                color = BrandAccent,
                                fontSize = 10.5.sp
                            )
                        }

                        // Options Popup (Rename, Pin, Delete)
                        IconButton(
                            onClick = {
                                renameDialogChat = chat
                                newTitleInput = chat.title
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        IconButton(
                            onClick = { onTogglePin(chat.id, !chat.isPinned) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_pin),
                                contentDescription = null,
                                tint = if (chat.isPinned) BrandCyan else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    renameDialogChat?.let { chatToRename ->
        AlertDialog(
            onDismissRequest = { renameDialogChat = null },
            title = { Text(stringResource(R.string.rename_chat_title), color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newTitleInput,
                    onValueChange = { newTitleInput = it },
                    placeholder = { Text(stringResource(R.string.enter_new_title)) }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitleInput.isNotBlank()) {
                            onRenameChat(chatToRename.id, newTitleInput.trim())
                        }
                        renameDialogChat = null
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogChat = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = Color(0xFF1E1738)
        )
    }
}
