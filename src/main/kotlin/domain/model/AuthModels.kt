package com.example.domain.model

data class RegisterProfile(
    val email: String,
    val password: String,
    val displayName: String?
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

data class AuthResult(
    val tokens: AuthTokens,
    val profile: ProfileEntity
)
