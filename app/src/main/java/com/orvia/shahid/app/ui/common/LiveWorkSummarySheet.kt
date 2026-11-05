package com.orvia.shahid.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orvia.shahid.app.R
import com.orvia.shahid.app.domain.model.StepStatus
import com.orvia.shahid.app.domain.model.WorkStep
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandCyan
import com.orvia.shahid.app.ui.theme.BrandPrimary
import com.orvia.shahid.app.ui.theme.DiffAddGreen
import com.orvia.shahid.app.ui.theme.DiffRemoveRed

@Composable
fun StickyLiveSummaryBar(
    currentStepText: String,
    stepProgress: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1738))
            .border(0.5.dp, Color(0xFF3B2E63))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(BrandCyan, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currentStepText,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = stepProgress,
            color = BrandAccent,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveWorkSummarySheet(
    steps: List<WorkStep>,
    isThinking: Boolean,
    thinkingElapsedSeconds: Int,
    onDismiss: () -> Unit,
    onFileClick: ((String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFF130E24),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.summary_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = stringResource(R.string.clear_text),
                        tint = Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Steps Timeline
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                itemsIndexed(steps) { index, step ->
                    val isLast = index == steps.lastIndex && !isThinking

                    TimelineStepItem(
                        step = step,
                        isLast = isLast,
                        onFileClick = onFileClick
                    )
                }

                if (isThinking) {
                    item {
                        ThinkingTimelineItem(seconds = thinkingElapsedSeconds)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimelineStepItem(
    step: WorkStep,
    isLast: Boolean,
    onFileClick: ((String) -> Unit)?
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Vertical Timeline Column (Dot + Connecting line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            val dotColor = when (step.status) {
                StepStatus.COMPLETED -> Color.Gray
                StepStatus.RUNNING -> BrandCyan
                StepStatus.THINKING -> BrandAccent
            }

            Box(
                modifier = Modifier
                    .size(if (step.status == StepStatus.RUNNING) 12.dp else 10.dp)
                    .background(dotColor, CircleShape)
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                        .background(Color(0xFF332A52))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Step Content
        Column(modifier = Modifier.weight(1f).padding(bottom = 12.dp)) {
            val textWeight = if (step.status == StepStatus.RUNNING) FontWeight.Bold else FontWeight.Normal
            val textColor = if (step.status == StepStatus.RUNNING) Color.White else Color(0xFFC7C5D3)

            Text(
                text = step.title,
                fontWeight = textWeight,
                fontSize = 13.5.sp,
                color = textColor
            )

            // File step info
            step.fileName?.let { fName ->
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { onFileClick?.invoke(fName) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_file),
                        contentDescription = null,
                        tint = BrandAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = fName,
                        fontSize = 12.sp,
                        color = BrandAccent,
                        fontWeight = FontWeight.Medium
                    )

                    if (step.linesAdded != null || step.linesRemoved != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+${step.linesAdded ?: 0} / -${step.linesRemoved ?: 0}",
                            fontSize = 11.sp,
                            color = if ((step.linesAdded ?: 0) > 0) DiffAddGreen else DiffRemoveRed
                        )
                    }
                }
            }

            // Error & Fix info
            step.errorDetails?.let { err ->
                Text(
                    text = "Error: $err",
                    color = DiffRemoveRed,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ThinkingTimelineItem(seconds: Int) {
    val transition = rememberInfiniteTransition(label = "ThinkingAnim")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ThinkingAlpha"
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(BrandPrimary, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.thinking_shimmer),
                color = BrandAccent.copy(alpha = alpha),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${seconds}s",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
