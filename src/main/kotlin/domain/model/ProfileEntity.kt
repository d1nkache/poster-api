package com.example.domain.model

data class ProfileEntity(
    val userId: Long,
    val name: String,
    val username: String,
    val email: String,
    val birthday: String?,
    val bio: String?,
    val avatarUrl: String,
    val isOnline: Boolean
)
