package com.example.domain.model

data class MessageEntity(
    val id: Long,
    val chatId: Long,
    val bodyText: String,
    val direction: MessageDirection,
    val status: MessageStatus,
    val isRead: Boolean,
    val createdAt: String,
    val sentAt: String?,
    val receivedAt: String?
)
