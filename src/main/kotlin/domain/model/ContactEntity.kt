package com.example.domain.model

data class ContactEntity(
    val id: Long,
    val profileId: Long,
    val email: String,
    val displayName: String?,
    val avatarUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
