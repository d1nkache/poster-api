package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class UploadProfileAvatarResponse(
    val avatarUrl: String
)
