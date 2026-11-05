package com.orvia.shahid.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orvia.shahid.app.data.local.dao.BuilderProjectDao
import com.orvia.shahid.app.data.local.entities.BuilderProjectEntity
import com.orvia.shahid.app.data.local.entities.BuilderVersionEntity
import com.orvia.shahid.app.domain.diff.PatchApplier
import com.orvia.shahid.app.domain.model.AdaptiveQuestion
import com.orvia.shahid.app.domain.model.BuilderProject
import com.orvia.shahid.app.domain.model.ChatMessage
import com.orvia.shahid.app.domain.model.MessageRole
import com.orvia.shahid.app.domain.model.QuestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuilderRepository @Inject constructor(
    private val builderProjectDao: BuilderProjectDao,
    private val providerManager: ProviderManager,
    private val gson: Gson = Gson()
) {

    fun getProjectForChat(chatId: String): Flow<BuilderProject?> =
        builderProjectDao.getProjectForChat(chatId).map { it?.toDomain(gson) }

    suspend fun getNextQuestion(
        idea: String,
        previousAnswers: List<Pair<String, String>>,
        currentQuestionNumber: Int
    ): AdaptiveQuestion = withContext(Dispatchers.IO) {
        val totalTarget = 15
        val systemPrompt = """
            You are an expert software architect conducting an adaptive project scoping interview.
            The user wants to build: "$idea".
            Current question index: $currentQuestionNumber of $totalTarget.
            Previous QA:
            ${previousAnswers.joinToString("\n") { (q, a) -> "Q: $q\nA: $a" }}

            Respond with ONLY a JSON object in this exact schema:
            {
              "questionNumber": $currentQuestionNumber,
              "totalQuestions": $totalTarget,
              "isComplete": ${currentQuestionNumber >= totalTarget},
              "question": "Your adaptive question here",
              "questionType": "SINGLE_CHOICE",
              "options": ["Option 1", "Option 2", "Option 3"],
              "helpText": "Helpful guidance or reason for this question"
            }
        """.trimIndent()

        val messages = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                chatId = "temp",
                role = MessageRole.USER,
                content = "Generate question $currentQuestionNumber."
            )
        )

        val jsonBuffer = StringBuilder()
        providerManager.streamChatWithFallback(messages, systemPrompt = systemPrompt).collect { chunk ->
            jsonBuffer.append(chunk.text)
        }

        val raw = jsonBuffer.toString().trim()
        val cleanedJson = raw.substringAfter("{").substringBeforeLast("}")
        val validJson = "{$cleanedJson}"

        try {
            gson.fromJson(validJson, AdaptiveQuestion::class.java)
        } catch (e: Exception) {
            AdaptiveQuestion(
                questionNumber = currentQuestionNumber,
                totalQuestions = totalTarget,
                isComplete = currentQuestionNumber >= totalTarget,
                question = "What additional requirements or libraries do you need for '$idea'?",
                questionType = QuestionType.FREE_TEXT,
                options = emptyList(),
                helpText = "Specify any third-party dependencies, UI components, or logic."
            )
        }
    }

    suspend fun generateProject(
        chatId: String,
        projectName: String,
        idea: String,
        qaTranscript: List<Pair<String, String>>,
        onStepUpdate: (String) -> Unit = {}
    ): BuilderProject = withContext(Dispatchers.IO) {
        onStepUpdate("Synthesizing architecture specifications…")

        val systemPrompt = """
            You are an elite autonomous full-stack software engineer.
            Generate the complete, production-ready codebase for: "$projectName".
            User idea: "$idea".
            Specifications:
            ${qaTranscript.joinToString("\n") { (q, a) -> "- $q: $a" }}

            Format your response strictly using file headers:
            ### FILE: path/to/file.ext
            ```
            <full file content>
            ```
            Generate all necessary files including configs, source code, build scripts, and documentation.
        """.trimIndent()

        val messages = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                role = MessageRole.USER,
                content = "Build the project now."
            )
        )

        onStepUpdate("Generating project source files…")
        val contentBuilder = StringBuilder()
        providerManager.streamChatWithFallback(messages, systemPrompt = systemPrompt).collect { chunk ->
            contentBuilder.append(chunk.text)
        }

        val files = parseProjectFiles(contentBuilder.toString())
        val projectType = detectProjectType(files)

        val entity = BuilderProjectEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            projectName = projectName,
            projectType = projectType,
            filesJson = gson.toJson(files),
            version = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        builderProjectDao.insertOrUpdateProject(entity)
        entity.toDomain(gson)
    }

    suspend fun applyProjectPatch(
        chatId: String,
        userPromptOrError: String,
        onStepUpdate: (String) -> Unit = {}
    ): BuilderProject = withContext(Dispatchers.IO) {
        val existing = builderProjectDao.getProjectForChatDirect(chatId)
            ?: throw IllegalStateException("No active builder project found for chat $chatId")

        val currentFiles: Map<String, String> = gson.fromJson(
            existing.filesJson,
            object : TypeToken<Map<String, String>>() {}.type
        )

        onStepUpdate("Analyzing code changes and preparing patch…")
        val systemPrompt = """
            You are an expert software engineer updating an existing project.
            Existing files:
            ${currentFiles.keys.joinToString("\n") { "- $it" }}

            The user asks: "$userPromptOrError"
            Provide ONLY the modified or new files formatted as:
            ### FILE: path/to/file.ext
            ```
            <updated content>
            ```
        """.trimIndent()

        val messages = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                role = MessageRole.USER,
                content = userPromptOrError
            )
        )

        val patchBuilder = StringBuilder()
        providerManager.streamChatWithFallback(messages, systemPrompt = systemPrompt).collect { chunk ->
            patchBuilder.append(chunk.text)
        }

        val newFiles = parseProjectFiles(patchBuilder.toString())
        val updatedFiles = currentFiles.toMutableMap()

        newFiles.forEach { (path, content) ->
            val orig = currentFiles[path] ?: ""
            updatedFiles[path] = PatchApplier.applyPatch(orig, content)
        }

        val newVersion = existing.version + 1
        val updatedEntity = existing.copy(
            filesJson = gson.toJson(updatedFiles),
            version = newVersion,
            updatedAt = System.currentTimeMillis()
        )

        builderProjectDao.insertOrUpdateProject(updatedEntity)
        builderProjectDao.insertVersion(
            BuilderVersionEntity(
                id = UUID.randomUUID().toString(),
                projectId = existing.id,
                versionNumber = newVersion,
                patchSummary = userPromptOrError.take(100),
                diffJson = gson.toJson(newFiles.keys.toList()),
                timestamp = System.currentTimeMillis()
            )
        )

        updatedEntity.toDomain(gson)
    }

    fun createProjectZip(context: Context, project: BuilderProject): File {
        val zipFile = File(context.cacheDir, "${project.projectName.replace(" ", "_")}-v${project.version}.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            project.files.forEach { (path, content) ->
                val entry = ZipEntry(path)
                zos.putNextEntry(entry)
                zos.write(content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }
        return zipFile
    }

    private fun parseProjectFiles(raw: String): Map<String, String> {
        val files = mutableMapOf<String, String>()
        val fileRegex = Regex("### FILE:\\s*([^\n\r]+)[\\r\\n]+```[a-zA-Z]*[\\r\\n]+([\\s\\S]*?)```", RegexOption.MULTILINE)
        val matches = fileRegex.findAll(raw).toList()

        for (match in matches) {
            val path = match.groupValues[1].trim()
            val content = match.groupValues[2]
            if (path.isNotBlank()) {
                files[path] = content
            }
        }

        if (files.isEmpty() && raw.contains("```")) {
            files["main_output.txt"] = raw
        }
        return files
    }

    private fun detectProjectType(files: Map<String, String>): String {
        return when {
            files.keys.any { it.endsWith(".kt") || it.contains("AndroidManifest.xml") } -> "ANDROID"
            files.keys.any { it.endsWith(".php") } -> "BACKEND"
            files.keys.any { it.endsWith(".html") || it.endsWith(".css") || it.endsWith(".js") } -> "WEB"
            else -> "CODE"
        }
    }

    private fun BuilderProjectEntity.toDomain(gson: Gson): BuilderProject {
        val filesMap: Map<String, String> = gson.fromJson(
            filesJson,
            object : TypeToken<Map<String, String>>() {}.type
        ) ?: emptyMap()
        return BuilderProject(
            id = id,
            chatId = chatId,
            projectName = projectName,
            projectType = projectType,
            files = filesMap,
            version = version,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
