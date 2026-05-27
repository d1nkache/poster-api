package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class MessageListResponse(
    val items: List<MessageResponse>
)
