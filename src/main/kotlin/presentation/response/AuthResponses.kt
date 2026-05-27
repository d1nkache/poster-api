package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val message: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val profile: ProfileResponse
)
