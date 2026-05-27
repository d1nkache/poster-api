package com.example.presentation.response

import com.example.domain.model.MessageDirection
import com.example.domain.model.MessageStatus
import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val id: Long,
    val chatId: Long,
    val bodyText: String,
    val direction: MessageDirection,
    val status: MessageStatus,
    val isRead: Boolean,
    val createdAt: String,
    val sentAt: String? = null,
    val receivedAt: String? = null
)
