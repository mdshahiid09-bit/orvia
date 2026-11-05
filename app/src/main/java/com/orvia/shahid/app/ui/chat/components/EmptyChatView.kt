package com.orvia.shahid.app.ui.chat.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orvia.shahid.app.R
import com.orvia.shahid.app.domain.model.ChatMode
import com.orvia.shahid.app.ui.common.AnimatedOrb
import com.orvia.shahid.app.ui.common.ParticleBackground
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandCyan
import java.util.Calendar

@Composable
fun EmptyChatView(
    currentMode: ChatMode,
    animationsEnabled: Boolean,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour in 4..11 -> stringResource(R.string.greeting_morning)
        hour in 12..16 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }

    val subtitle = when (currentMode) {
        ChatMode.ASK_ANYTHING -> stringResource(R.string.empty_chat_subtitle_ask)
        ChatMode.BUILDER -> stringResource(R.string.empty_chat_subtitle_builder)
        ChatMode.MASTER_PROMPT -> stringResource(R.string.empty_chat_subtitle_master_prompt)
    }

    val suggestions = when (currentMode) {
        ChatMode.ASK_ANYTHING -> listOf(
            stringResource(R.string.suggestion_ask_1),
            stringResource(R.string.suggestion_ask_2),
            stringResource(R.string.suggestion_ask_3)
        )
        ChatMode.BUILDER -> listOf(
            stringResource(R.string.suggestion_builder_1),
            stringResource(R.string.suggestion_builder_2),
            stringResource(R.string.suggestion_builder_3)
        )
        ChatMode.MASTER_PROMPT -> listOf(
            stringResource(R.string.suggestion_prompt_1),
            stringResource(R.string.suggestion_prompt_2),
            stringResource(R.string.suggestion_prompt_3)
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ParticleBackground(enabled = animationsEnabled)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedOrb(
                size = 72.dp,
                animate = animationsEnabled
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$greeting, explore Orvia",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 13.5.sp,
                color = Color(0xFFA6A3BC),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Suggestion Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                suggestions.forEach { suggestion ->
                    SuggestionCard(
                        text = suggestion,
                        onClick = { onSuggestionClick(suggestion) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF17112C))
            .border(1.dp, Color(0xFF2E2452), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFFE2E0EE),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = BrandAccent,
            modifier = Modifier.size(16.dp)
        )
    }
}
