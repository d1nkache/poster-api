package com.example.presentation.request

import kotlinx.serialization.Serializable

@Serializable
data class SaveMailAccessTokenRequest(
    val token: String,
    val smtpHost: String? = null,
    val smtpPort: Int? = null,
    val imapHost: String? = null,
    val imapPort: Int? = null
)
