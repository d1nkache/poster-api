package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageResponse(
    val message: MessageResponse
)
