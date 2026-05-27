package com.example.domain.service

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PasswordHasher {
    fun hash(value: String): String {
        val salt = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(salt)
        val hash = pbkdf2(value, salt)

        return listOf(ALGORITHM, ITERATIONS.toString(), encode(salt), encode(hash)).joinToString("$")
    }

    fun verify(value: String, encodedHash: String?): Boolean {
        if (encodedHash.isNullOrBlank()) {
            return false
        }

        val parts = encodedHash.split("$")
        if (parts.size != 4 || parts[0] != ALGORITHM) {
            return false
        }

        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = decode(parts[2])
        val expectedHash = decode(parts[3])
        val actualHash = pbkdf2(value, salt, iterations)

        return expectedHash.contentEquals(actualHash)
    }

    private fun pbkdf2(value: String, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray {
        val spec = PBEKeySpec(value.toCharArray(), salt, iterations, KEY_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private fun encode(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun decode(value: String): ByteArray {
        return Base64.getDecoder().decode(value)
    }

    private companion object {
        const val ALGORITHM = "pbkdf2-sha256"
        const val ITERATIONS = 120_000
        const val SALT_BYTES = 16
        const val KEY_BITS = 256
        val secureRandom = SecureRandom()
    }
}
