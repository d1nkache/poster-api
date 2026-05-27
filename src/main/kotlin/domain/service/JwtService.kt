package com.example.domain.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.domain.model.AuthTokens
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Date

class JwtService {
    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .build()

    fun createTokens(profileId: Long, email: String): AuthTokens {
        val refreshToken = generateRefreshToken()

        return AuthTokens(
            accessToken = createAccessToken(profileId, email),
            refreshToken = refreshToken
        )
    }

    fun createAccessToken(profileId: Long, email: String): String {
        val expiresAt = Instant.now().plus(ACCESS_TOKEN_MINUTES, ChronoUnit.MINUTES)

        return JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withSubject(profileId.toString())
            .withClaim("email", email)
            .withClaim("type", "access")
            .withExpiresAt(Date.from(expiresAt))
            .sign(algorithm)
    }

    private fun generateRefreshToken(): String {
        val bytes = ByteArray(REFRESH_TOKEN_BYTES)
        secureRandom.nextBytes(bytes)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    companion object {
        const val AUDIENCE = "poster-android"
        const val ISSUER = "poster-api"
        const val REALM = "poster-api"
        private const val ACCESS_TOKEN_MINUTES = 60L
        private const val REFRESH_TOKEN_BYTES = 48
        private const val SECRET = "change-me-poster-secret"
        private val algorithm = Algorithm.HMAC256(SECRET)
        private val secureRandom = SecureRandom()
    }
}
