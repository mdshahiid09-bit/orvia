package com.orvia.shahid.app.ui.chat.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.orvia.shahid.app.R
import com.orvia.shahid.app.domain.model.ChatMessage
import com.orvia.shahid.app.domain.model.MessageRole
import com.orvia.shahid.app.ui.common.MarkdownText
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandCyan
import com.orvia.shahid.app.ui.theme.BrandPrimary
import com.orvia.shahid.app.ui.theme.DiffRemoveRed
import com.orvia.shahid.app.ui.theme.UserBubbleDark

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: ChatMessage,
    onEditAndResend: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showActionMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = { showActionMenu = true }
            )
    ) {
        if (message.role == MessageRole.USER) {
            UserMessageBubble(message = message)
        } else {
            AiMessageContent(message = message, onRetry = onRetry)
        }
    }

    if (showActionMenu) {
        MessageActionBottomSheet(
            message = message,
            onDismiss = { showActionMenu = false },
            onEditAndResend = onEditAndResend
        )
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(UserBubbleDark)
                .border(1.dp, Color(0xFF3E3167), RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = message.content,
                fontSize = 14.5.sp,
                color = Color.White,
                lineHeight = 21.sp
            )

            // Attached images
            if (message.imageUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                message.imageUris.forEach { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun AiMessageContent(
    message: ChatMessage,
    onRetry: () -> Unit
) {
    var thinkingExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        // Thinking Collapsible Card (if model reasoning is present)
        if (!message.rawReasoning.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140F26))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = { thinkingExpanded = !thinkingExpanded }, onLongClick = {}),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_thinking),
                                contentDescription = null,
                                tint = BrandAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.thinking_toggle),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandAccent
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = thinkingExpanded) {
                        Text(
                            text = message.rawReasoning,
                            fontSize = 12.sp,
                            color = Color(0xFFB5B2C7),
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Markdown Formatted Message
        MarkdownText(
            markdown = message.content,
            textColor = Color(0xFFF0EFF5)
        )

        // Error Card (if generation error occurred)
        if (message.isError) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1318)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.error_card_title),
                            color = DiffRemoveRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = message.content,
                            color = Color(0xFFFFB4AB),
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_retry), contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.retry_action), fontSize = 11.sp)
                    }
                }
            }
        }

        // Footer: Latency, Token Count, Provider Name
        val totalTokens = (message.inputTokens ?: 0) + (message.outputTokens ?: 0)
        val responseSec = (message.responseTimeMs ?: 0L) / 1000.0
        val latencyFormatted = "%.1fs".format(responseSec)
        val providerTag = message.providerUsed ?: message.modelUsed ?: "Orvia AI"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    R.string.response_info_format,
                    latencyFormatted,
                    totalTokens,
                    providerTag
                ),
                fontSize = 11.sp,
                color = Color(0xFF7E7A91)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageActionBottomSheet(
    message: ChatMessage,
    onDismiss: () -> Unit,
    onEditAndResend: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val copiedText = stringResource(R.string.toast_copied)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF140F26),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.content))
                            Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        onLongClick = {}
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_copy), contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Text(stringResource(R.string.copy_message), fontSize = 15.sp, color = Color.White)
            }

            // Edit & Resend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            onEditAndResend(message.content)
                            onDismiss()
                        },
                        onLongClick = {}
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_edit), contentDescription = null, tint = BrandAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Text(stringResource(R.string.edit_message), fontSize = 15.sp, color = Color.White)
            }

            // Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, message.content)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share message"))
                            onDismiss()
                        },
                        onLongClick = {}
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_share), contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Text(stringResource(R.string.share_message), fontSize = 15.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
