package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class MailAccessTokenStatusResponse(
    val configured: Boolean
)
