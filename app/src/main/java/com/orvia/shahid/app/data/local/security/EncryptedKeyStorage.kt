package com.orvia.shahid.app.data.local.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptedKeyStorage(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        getOrCreateKey()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            return keyGenerator.generateKey()
        }

        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val secretKey = getOrCreateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(1 + iv.size + cipherBytes.size)
            combined[0] = iv.size.toByte()
            System.arraycopy(iv, 0, combined, 1, iv.size)
            System.arraycopy(cipherBytes, 0, combined, 1 + iv.size, cipherBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    fun decrypt(cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        return try {
            val combined = Base64.decode(cipherText, Base64.NO_WRAP)
            if (combined.isEmpty()) return ""

            val ivLen = combined[0].toInt()
            if (ivLen <= 0 || ivLen >= combined.size) {
                return String(combined, Charsets.UTF_8)
            }

            val iv = ByteArray(ivLen)
            System.arraycopy(combined, 1, iv, 0, ivLen)

            val cipherBytesLen = combined.size - 1 - ivLen
            val cipherBytes = ByteArray(cipherBytesLen)
            System.arraycopy(combined, 1 + ivLen, cipherBytes, 0, cipherBytesLen)

            val secretKey = getOrCreateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val plainBytes = cipher.doFinal(cipherBytes)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            try {
                String(Base64.decode(cipherText, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (ex: Exception) {
                cipherText
            }
        }
    }

    fun getGeminiApiKey(): String = decrypt(prefs.getString(KEY_GEMINI, "") ?: "")
    fun setGeminiApiKey(key: String) = prefs.edit().putString(KEY_GEMINI, encrypt(key.trim())).apply()

    fun getGroqApiKey(): String = decrypt(prefs.getString(KEY_GROQ, "") ?: "")
    fun setGroqApiKey(key: String) = prefs.edit().putString(KEY_GROQ, encrypt(key.trim())).apply()

    fun getOpenRouterApiKey(): String = decrypt(prefs.getString(KEY_OPENROUTER, "") ?: "")
    fun setOpenRouterApiKey(key: String) = prefs.edit().putString(KEY_OPENROUTER, encrypt(key.trim())).apply()

    fun getGitHubToken(): String = decrypt(prefs.getString(KEY_GITHUB, "") ?: "")
    fun setGitHubToken(token: String) = prefs.edit().putString(KEY_GITHUB, encrypt(token.trim())).apply()

    fun clearAllKeys() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "orvia_secure_prefs"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "OrviaMasterKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private const val KEY_GEMINI = "enc_gemini_key"
        private const val KEY_GROQ = "enc_groq_key"
        private const val KEY_OPENROUTER = "enc_openrouter_key"
        private const val KEY_GITHUB = "enc_github_token"
    }
}
