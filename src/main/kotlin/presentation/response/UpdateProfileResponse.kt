package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileResponse(
    val userId: Long,
    val username: String,
    val bio: String?,
    val avatarUrl: String
)
