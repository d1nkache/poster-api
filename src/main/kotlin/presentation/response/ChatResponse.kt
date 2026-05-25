package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val id: String,
    val title: String,
    val initials: String,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val unreadCount: Int,
    val isOnline: Boolean
)
