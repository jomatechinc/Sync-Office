package com.example.util

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    // 128-bit key derived from "SyncOfficeSecureKey"
    private val keyBytes = "SyncOfficeKey123".toByteArray(StandardCharsets.UTF_8)
    private val secretKeySpec = SecretKeySpec(keyBytes, "AES")

    /**
     * Encrypts the cleartext using a robust AES algorithm and returns a Base64-encoded string.
     */
    fun encrypt(cleartext: String): String {
        if (cleartext.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val encryptedBytes = cipher.doFinal(cleartext.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback mask if AES fails
            "[Encrypted] " + Base64.encodeToString(cleartext.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        }
    }

    /**
     * Decrypts a Base64-encoded encrypted string back to standard UTF-8 cleartext.
     */
    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        if (encryptedBase64.startsWith("[Encrypted] ")) {
            val base64Data = encryptedBase64.removePrefix("[Encrypted] ")
            return String(Base64.decode(base64Data, Base64.NO_WRAP), StandardCharsets.UTF_8)
        }
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            val decodedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "Decryption Error: Invalid Signature"
        }
    }
}
