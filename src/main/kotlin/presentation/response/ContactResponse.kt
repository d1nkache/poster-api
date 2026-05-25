package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class ContactResponse(
    val id: Long,
    val email: String,
    val displayName: String?,
    val avatarUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
