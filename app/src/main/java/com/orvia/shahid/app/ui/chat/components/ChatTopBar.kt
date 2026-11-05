package com.orvia.shahid.app.ui.chat.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orvia.shahid.app.R
import com.orvia.shahid.app.domain.model.ChatMode
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandCyan
import com.orvia.shahid.app.ui.theme.BrandPrimary

@Composable
fun ChatTopBar(
    currentMode: ChatMode,
    modelName: String,
    onModeSelect: (ChatMode) -> Unit,
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val switchHint = stringResource(R.string.model_switch_hint)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0B1E))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Top Row: Menu, Model Chip, New Chat, Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = stringResource(R.string.drawer_title_chats),
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Model Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF221A3D))
                        .border(1.dp, Color(0xFF3B2E63), RoundedCornerShape(16.dp))
                        .clickable { Toast.makeText(context, switchHint, Toast.LENGTH_SHORT).show() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(BrandCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = modelName.take(18),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNewChatClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = stringResource(R.string.action_new_chat),
                        tint = BrandAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = stringResource(R.string.settings_title),
                        tint = Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(top = 4.dp))

        // Segmented Mode Toggle (Ask Anything | Builder | Master Prompt)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B1433), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF2F2454), RoundedCornerShape(10.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ModeSegmentTab(
                title = stringResource(R.string.mode_ask_anything),
                iconRes = R.drawable.ic_ask_anything,
                isSelected = currentMode == ChatMode.ASK_ANYTHING,
                onClick = { onModeSelect(ChatMode.ASK_ANYTHING) },
                modifier = Modifier.weight(1f)
            )

            ModeSegmentTab(
                title = stringResource(R.string.mode_builder),
                iconRes = R.drawable.ic_builder,
                isSelected = currentMode == ChatMode.BUILDER,
                onClick = { onModeSelect(ChatMode.BUILDER) },
                modifier = Modifier.weight(1f)
            )

            ModeSegmentTab(
                title = stringResource(R.string.mode_master_prompt),
                iconRes = R.drawable.ic_master_prompt,
                isSelected = currentMode == ChatMode.MASTER_PROMPT,
                onClick = { onModeSelect(ChatMode.MASTER_PROMPT) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeSegmentTab(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) BrandPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (isSelected) Color.White else Color(0xFFA8A5BC),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFFA8A5BC)
        )
    }
}
