package com.orvia.shahid.app.data.repository

import com.google.gson.Gson
import com.orvia.shahid.app.data.local.dao.MasterPromptVersionDao
import com.orvia.shahid.app.data.local.entities.MasterPromptVersionEntity
import com.orvia.shahid.app.domain.diff.DiffResult
import com.orvia.shahid.app.domain.diff.PatchApplier
import com.orvia.shahid.app.domain.model.AdaptiveQuestion
import com.orvia.shahid.app.domain.model.ChatMessage
import com.orvia.shahid.app.domain.model.MasterPromptVersion
import com.orvia.shahid.app.domain.model.MessageRole
import com.orvia.shahid.app.domain.model.QuestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterPromptRepository @Inject constructor(
    private val versionDao: MasterPromptVersionDao,
    private val providerManager: ProviderManager,
    private val gson: Gson = Gson()
) {

    fun getVersionsForChat(chatId: String): Flow<List<MasterPromptVersion>> =
        versionDao.getVersionsForChat(chatId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getLatestVersion(chatId: String): MasterPromptVersion? = withContext(Dispatchers.IO) {
        versionDao.getLatestVersionForChat(chatId)?.toDomain()
    }

    suspend fun getNextQuestion(
        idea: String,
        qaHistory: List<Pair<String, String>>,
        currentNumber: Int
    ): AdaptiveQuestion = withContext(Dispatchers.IO) {
        val total = 16
        val systemPrompt = """
            You are a master prompt engineering specialist conducting an adaptive interview.
            Target project: "$idea".
            Current question: $currentNumber of $total.
            Previous QA:
            ${qaHistory.joinToString("\n") { (q, a) -> "Q: $q\nA: $a" }}

            Respond with ONLY JSON matching:
            {
              "questionNumber": $currentNumber,
              "totalQuestions": $total,
              "isComplete": ${currentNumber >= total},
              "question": "Question text",
              "questionType": "SINGLE_CHOICE",
              "options": ["A", "B", "C"],
              "helpText": "Explanation"
            }
        """.trimIndent()

        val messages = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                chatId = "prompt_interview",
                role = MessageRole.USER,
                content = "Generate question $currentNumber"
            )
        )

        val buffer = StringBuilder()
        providerManager.streamChatWithFallback(messages, systemPrompt = systemPrompt).collect { chunk ->
            buffer.append(chunk.text)
        }

        val raw = buffer.toString().trim()
        val cleaned = raw.substringAfter("{").substringBeforeLast("}")
        val validJson = "{$cleaned}"

        try {
            gson.fromJson(validJson, AdaptiveQuestion::class.java)
        } catch (e: Exception) {
            AdaptiveQuestion(
                questionNumber = currentNumber,
                totalQuestions = total,
                isComplete = currentNumber >= total,
                question = "What constraints, guidelines, or quality bars must be enforced?",
                questionType = QuestionType.FREE_TEXT,
                options = emptyList(),
                helpText = "Include performance targets, privacy rules, or tech preferences."
            )
        }
    }

    suspend fun generateMasterPrompt(
        chatId: String,
        chatTitle: String,
        idea: String,
        qaHistory: List<Pair<String, String>>,
        onStepUpdate: (String) -> Unit = {}
    ): MasterPromptVersion = withContext(Dispatchers.IO) {
        onStepUpdate("Synthesizing interview specifications into Master Prompt…")

        val sanitizedTitle = chatTitle.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-')
        val fileName = if (sanitizedTitle.isNotBlank()) "$sanitizedTitle.md" else "master_prompt.md"

        val systemPrompt = """
            You are the world's leading Prompt Architect.
            Create a comprehensive, production-grade Master Prompt in English for: "$idea".
            Interview transcript:
            ${qaHistory.joinToString("\n") { (q, a) -> "- $q: $a" }}

            Structure:
            # Master Prompt: [App/Project Name]
            ## 0. Role & Operating Principles
            ## 1. Project Overview & Architecture
            ## 2. Technical Stack & Dependencies
            ## 3. Core Features & User Flows
            ## 4. UI/UX & Design Guidelines
            ## 5. Security, State & Error Handling
            ## 6. Implementation Checklist & Verification
        """.trimIndent()

        val messages = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                role = MessageRole.USER,
                content = "Generate the complete master prompt in English."
            )
        )

        val promptBuffer = StringBuilder()
        providerManager.streamChatWithFallback(messages, systemPrompt = systemPrompt).collect { chunk ->
            promptBuffer.append(chunk.text)
        }

        val content = promptBuffer.toString().trim()
        val entity = MasterPromptVersionEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            versionNumber = 1,
            versionName = "v1",
            fileName = fileName,
            content = content,
            patchDiff = null,
            timestamp = System.currentTimeMillis()
        )

        versionDao.insertVersion(entity)
        entity.toDomain()
    }

    suspend fun previewEdit(
        chatId: String,
        userInstruction: String
    ): Pair<String, DiffResult> = withContext(Dispatchers.IO) {
        val latest = versionDao.getLatestVersionForChat(chatId)
            ?: throw IllegalStateException("No master prompt exists for chat $chatId")

        val systemPrompt = """
            You are an expert prompt editor.
            You have this master prompt:
            ${latest.content}

            The user asks to change or refine: "$userInstruction"
            Update the prompt in-place. Output the entire updated markdown master prompt.
        """.trimIndent()

        val messages = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                role = MessageRole.USER,
                content = userInstruction
            )
        )

        val updatedBuffer = StringBuilder()
        providerManager.streamChatWithFallback(messages, systemPrompt = systemPrompt).collect { chunk ->
            updatedBuffer.append(chunk.text)
        }

        val updatedContent = updatedBuffer.toString().trim()
        val diff = PatchApplier.computeDiff(latest.content, updatedContent)
        Pair(updatedContent, diff)
    }

    suspend fun saveEditedVersion(
        chatId: String,
        updatedContent: String,
        diffSummary: String
    ): MasterPromptVersion = withContext(Dispatchers.IO) {
        val latest = versionDao.getLatestVersionForChat(chatId)
            ?: throw IllegalStateException("No master prompt found")

        val newVersionNum = latest.versionNumber + 1
        val newEntity = MasterPromptVersionEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            versionNumber = newVersionNum,
            versionName = "v$newVersionNum",
            fileName = latest.fileName,
            content = updatedContent,
            patchDiff = diffSummary,
            timestamp = System.currentTimeMillis()
        )

        versionDao.insertVersion(newEntity)
        newEntity.toDomain()
    }

    suspend fun undoToVersion(chatId: String, targetVersionNumber: Int): MasterPromptVersion = withContext(Dispatchers.IO) {
        val versions = versionDao.getLatestVersionForChat(chatId)
            ?: throw IllegalStateException("Version not found")

        val newEntity = MasterPromptVersionEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            versionNumber = versions.versionNumber + 1,
            versionName = "v${versions.versionNumber + 1} (Reverted)",
            fileName = versions.fileName,
            content = versions.content,
            patchDiff = "Reverted to version $targetVersionNumber",
            timestamp = System.currentTimeMillis()
        )
        versionDao.insertVersion(newEntity)
        newEntity.toDomain()
    }

    private fun MasterPromptVersionEntity.toDomain() = MasterPromptVersion(
        id = id,
        chatId = chatId,
        versionNumber = versionNumber,
        versionName = versionName,
        fileName = fileName,
        content = content,
        patchDiff = patchDiff,
        timestamp = timestamp
    )
}
