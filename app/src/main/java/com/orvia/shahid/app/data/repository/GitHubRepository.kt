package com.orvia.shahid.app.data.repository

import com.orvia.shahid.app.data.local.security.EncryptedKeyStorage
import com.orvia.shahid.app.data.remote.api.GitHubApiClient
import com.orvia.shahid.app.data.remote.models.GitHubUserDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubRepository @Inject constructor(
    private val gitHubApiClient: GitHubApiClient,
    private val keyStorage: EncryptedKeyStorage
) {

    suspend fun testToken(token: String? = null): Result<GitHubUserDto> {
        val effectiveToken = token ?: keyStorage.getGitHubToken()
        return gitHubApiClient.testToken(effectiveToken)
    }

    suspend fun createAndPushProject(
        repoName: String,
        isPrivate: Boolean,
        files: Map<String, String>,
        commitMessage: String = "Initial project commit from Orvia Builder"
    ): Result<String> {
        val token = keyStorage.getGitHubToken()
        if (token.isBlank()) {
            return Result.failure(IllegalStateException("GitHub personal access token is missing. Configure it in Settings."))
        }

        val userResult = gitHubApiClient.testToken(token)
        val user = userResult.getOrNull()
            ?: return Result.failure(userResult.exceptionOrNull() ?: RuntimeException("Invalid GitHub token"))

        val repoResult = gitHubApiClient.createRepository(token, repoName, isPrivate)
        if (repoResult.isFailure && repoResult.exceptionOrNull()?.message?.contains("422") != true) {
            return Result.failure(repoResult.exceptionOrNull() ?: RuntimeException("Failed to create GitHub repo"))
        }

        val pushResult = gitHubApiClient.pushFiles(token, user.login, repoName, files, commitMessage)
        return if (pushResult.isSuccess) {
            Result.success("https://github.com/${user.login}/$repoName")
        } else {
            Result.failure(pushResult.exceptionOrNull() ?: RuntimeException("Failed to push files to repository"))
        }
    }
}
