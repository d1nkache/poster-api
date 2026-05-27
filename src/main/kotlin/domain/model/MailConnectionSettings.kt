package com.example.domain.model

data class MailConnectionSettings(
    val token: String,
    val smtpHost: String?,
    val smtpPort: Int?,
    val imapHost: String?,
    val imapPort: Int?
)
