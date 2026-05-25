package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val birthday: String?,
    val bio: String?,
    val avatarUrl: String,
    val isOnline: Boolean
)
