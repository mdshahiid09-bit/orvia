package com.orvia.shahid.app.data.remote.api

import android.util.Base64
import com.google.gson.Gson
import com.orvia.shahid.app.data.remote.models.GitHubCreateFileRequest
import com.orvia.shahid.app.data.remote.models.GitHubCreateRepoRequest
import com.orvia.shahid.app.data.remote.models.GitHubRepoDto
import com.orvia.shahid.app.data.remote.models.GitHubUserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GitHubApiClient(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson = Gson()
) {

    suspend fun testToken(token: String): Result<GitHubUserDto> = withContext(Dispatchers.IO) {
        if (token.isBlank()) return@withContext Result.failure(IllegalArgumentException("GitHub token is empty"))
        try {
            val request = Request.Builder()
                .url("https://api.github.com/user")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Orvia-Android-App")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: ""
                val user = gson.fromJson(json, GitHubUserDto::class.java)
                Result.success(user)
            } else {
                Result.failure(RuntimeException("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRepository(
        token: String,
        name: String,
        isPrivate: Boolean,
        description: String = "Created with Orvia Builder"
    ): Result<GitHubRepoDto> = withContext(Dispatchers.IO) {
        try {
            val payload = GitHubCreateRepoRequest(
                name = name,
                description = description,
                isPrivate = isPrivate,
                autoInit = true
            )

            val request = Request.Builder()
                .url("https://api.github.com/user/repos")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Orvia-Android-App")
                .post(gson.toJson(payload).toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful || response.code == 422) {
                val json = response.body?.string() ?: ""
                val repo = gson.fromJson(json, GitHubRepoDto::class.java)
                Result.success(repo)
            } else {
                val err = response.body?.string() ?: response.message
                Result.failure(RuntimeException("HTTP ${response.code}: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushFiles(
        token: String,
        owner: String,
        repo: String,
        files: Map<String, String>,
        commitMessage: String = "Project update via Orvia Builder"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            for ((path, content) in files) {
                val base64 = Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                val checkRequest = Request.Builder()
                    .url("https://api.github.com/repos/$owner/$repo/contents/$path")
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Orvia-Android-App")
                    .get()
                    .build()

                var sha: String? = null
                val checkResponse = okHttpClient.newCall(checkRequest).execute()
                if (checkResponse.isSuccessful) {
                    val body = checkResponse.body?.string() ?: ""
                    val map = gson.fromJson(body, Map::class.java)
                    sha = map["sha"] as? String
                }

                val putPayload = GitHubCreateFileRequest(
                    message = commitMessage,
                    content = base64,
                    branch = "main",
                    sha = sha
                )

                val putRequest = Request.Builder()
                    .url("https://api.github.com/repos/$owner/$repo/contents/$path")
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Orvia-Android-App")
                    .put(gson.toJson(putPayload).toRequestBody("application/json".toMediaType()))
                    .build()

                val putResponse = okHttpClient.newCall(putRequest).execute()
                if (!putResponse.isSuccessful) {
                    val err = putResponse.body?.string() ?: putResponse.message
                    return@withContext Result.failure(RuntimeException("Failed to upload $path: HTTP ${putResponse.code} $err"))
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
