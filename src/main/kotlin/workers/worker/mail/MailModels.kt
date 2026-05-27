package com.example.workers.worker.mail

data class MailServerSettings(
    val email: String,
    val accessToken: String,
    val smtpHost: String,
    val smtpPort: Int,
    val imapHost: String,
    val imapPort: Int
)

data class OutgoingMail(
    val from: String,
    val to: String,
    val subject: String,
    val bodyText: String
)

data class IncomingMail(
    val uid: Long?,
    val providerMessageId: String?,
    val fromEmail: String,
    val fromDisplayName: String?,
    val subject: String?,
    val bodyText: String,
    val receivedAt: String
)
