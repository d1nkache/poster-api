package com.example.domain.service

import com.example.domain.model.MailConnectionSettings

object MailConnectionDefaults {
    fun resolveForEmail(
        email: String,
        settings: MailConnectionSettings
    ): MailConnectionSettings {
        val domain = email.substringAfter("@", "").lowercase()
        val provider = providers[domain]

        return settings.copy(
            smtpHost = settings.smtpHost ?: provider?.smtpHost ?: "smtp.$domain",
            smtpPort = settings.smtpPort ?: provider?.smtpPort ?: 587,
            imapHost = settings.imapHost ?: provider?.imapHost ?: "imap.$domain",
            imapPort = settings.imapPort ?: provider?.imapPort ?: 993
        )
    }

    private data class Provider(
        val smtpHost: String,
        val smtpPort: Int,
        val imapHost: String,
        val imapPort: Int
    )

    private val providers = mapOf(
        "gmail.com" to Provider("smtp.gmail.com", 587, "imap.gmail.com", 993),
        "googlemail.com" to Provider("smtp.gmail.com", 587, "imap.gmail.com", 993),
        "yandex.ru" to Provider("smtp.yandex.ru", 587, "imap.yandex.ru", 993),
        "ya.ru" to Provider("smtp.yandex.ru", 587, "imap.yandex.ru", 993),
        "mail.ru" to Provider("smtp.mail.ru", 465, "imap.mail.ru", 993),
        "outlook.com" to Provider("smtp-mail.outlook.com", 587, "outlook.office365.com", 993),
        "hotmail.com" to Provider("smtp-mail.outlook.com", 587, "outlook.office365.com", 993)
    )
}
