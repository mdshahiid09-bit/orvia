package com.orvia.shahid.app.domain.model

enum class QuestionType {
    SINGLE_CHOICE,
    MULTI_CHOICE,
    FREE_TEXT
}

data class AdaptiveQuestion(
    val questionNumber: Int,
    val totalQuestions: Int,
    val isComplete: Boolean,
    val question: String,
    val questionType: QuestionType,
    val options: List<String> = emptyList(),
    val helpText: String = ""
)
