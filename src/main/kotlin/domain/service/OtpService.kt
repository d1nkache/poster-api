package com.example.domain.service

import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit

class OtpService(
    private val passwordHasher: PasswordHasher
) {
    fun generate(): OtpChallenge {
        val code = secureRandom.nextInt(1_000_000).toString().padStart(6, '0')

        return OtpChallenge(
            code = code,
            codeHash = passwordHasher.hash(code),
            expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES).toString()
        )
    }

    fun verify(code: String, codeHash: String?, expiresAt: String?): Boolean {
        if (expiresAt == null || Instant.parse(expiresAt).isBefore(Instant.now())) {
            return false
        }

        return passwordHasher.verify(code, codeHash)
    }

    private companion object {
        val secureRandom = SecureRandom()
    }
}

data class OtpChallenge(
    val code: String,
    val codeHash: String,
    val expiresAt: String
)
