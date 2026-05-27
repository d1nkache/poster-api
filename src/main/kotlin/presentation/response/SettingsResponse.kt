package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class SettingsResponse(
    val language: String,
    val hasMailAccessToken: Boolean,
    val smtpHost: String?,
    val smtpPort: Int?,
    val imapHost: String?,
    val imapPort: Int?
)
