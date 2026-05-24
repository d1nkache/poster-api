package com.example.domain.model

data class ProfileEntity(
    val userId: Long,
    val username: String,
    val bio: String?,
    val avatarUrl: String
)
