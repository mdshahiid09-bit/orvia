package com.orvia.shahid.app.domain.model

enum class StepStatus {
    COMPLETED,
    RUNNING,
    THINKING
}

data class WorkStep(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val stepNumber: Int = 1,
    val totalSteps: Int = 1,
    val status: StepStatus = StepStatus.RUNNING,
    val fileName: String? = null,
    val linesAdded: Int? = null,
    val linesRemoved: Int? = null,
    val errorDetails: String? = null,
    val fixDetails: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
