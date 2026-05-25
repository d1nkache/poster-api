package com.example.domain.model

data class ChatEntity(
    val id: Long,
    val contactId: Long,
    val title: String,
    val isDeleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val contactEmail: String,
    val contactDisplayName: String?
)
