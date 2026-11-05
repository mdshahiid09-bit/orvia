package com.orvia.shahid.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orvia.shahid.app.R
import com.orvia.shahid.app.domain.model.AdaptiveQuestion
import com.orvia.shahid.app.domain.model.QuestionType
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandCyan
import com.orvia.shahid.app.ui.theme.BrandPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdaptiveQuestionBottomSheet(
    question: AdaptiveQuestion,
    onAnswerSubmitted: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedSingleOption by remember(question) { mutableStateOf<String?>(null) }
    val selectedMultiOptions = remember(question) { mutableStateListOf<String>() }
    var freeTextAnswer by remember(question) { mutableStateOf("") }

    val canProceed = when (question.questionType) {
        QuestionType.SINGLE_CHOICE -> selectedSingleOption != null
        QuestionType.MULTI_CHOICE -> selectedMultiOptions.isNotEmpty()
        QuestionType.FREE_TEXT -> freeTextAnswer.isNotBlank()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFF140F26),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Counter Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.question_counter, question.questionNumber, question.totalQuestions),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandCyan
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Title
            Text(
                text = question.question,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 24.sp
            )

            if (question.helpText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = question.helpText,
                    fontSize = 12.5.sp,
                    color = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Options based on QuestionType
            when (question.questionType) {
                QuestionType.SINGLE_CHOICE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        question.options.forEach { option ->
                            val isSelected = selectedSingleOption == option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) Color(0xFF322359) else Color(0xFF1D1735),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) BrandCyan else Color(0xFF382D5C),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedSingleOption = option }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check),
                                        contentDescription = null,
                                        tint = BrandCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Text(
                                    text = option,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFFE2E0EE)
                                )
                            }
                        }
                    }
                }

                QuestionType.MULTI_CHOICE -> {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        question.options.forEach { option ->
                            val isSelected = selectedMultiOptions.contains(option)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) Color(0xFF362562) else Color(0xFF1E1838),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) BrandAccent else Color(0xFF382E5C),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        if (isSelected) selectedMultiOptions.remove(option)
                                        else selectedMultiOptions.add(option)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 13.sp,
                                    color = if (isSelected) BrandCyan else Color.White
                                )
                            }
                        }
                    }
                }

                QuestionType.FREE_TEXT -> {
                    OutlinedTextField(
                        value = freeTextAnswer,
                        onValueChange = { freeTextAnswer = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.type_your_answer), color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandCyan,
                            unfocusedBorderColor = Color(0xFF392F5C),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = BrandCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = {
                    val finalAnswer = when (question.questionType) {
                        QuestionType.SINGLE_CHOICE -> selectedSingleOption ?: ""
                        QuestionType.MULTI_CHOICE -> selectedMultiOptions.joinToString(", ")
                        QuestionType.FREE_TEXT -> freeTextAnswer.trim()
                    }
                    onAnswerSubmitted(finalAnswer)
                },
                enabled = canProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    disabledContainerColor = Color(0xFF282046)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (question.isComplete) stringResource(R.string.action_finish) else stringResource(R.string.action_next),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
