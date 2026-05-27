package com.example.workers.worker.mail

import com.example.workers.worker.config.MailDefaults

class EmailProviderResolver(
    private val defaults: MailDefaults
) {
    fun resolve(email: String, accessToken: String): MailServerSettings {
        val domain = email.substringAfter("@", "").lowercase()
        val provider = providers[domain]

        return MailServerSettings(
            email = email,
            accessToken = accessToken,
            smtpHost = defaults.smtpHost ?: provider?.smtpHost ?: "smtp.$domain",
            smtpPort = defaults.smtpPort ?: provider?.smtpPort ?: 587,
            imapHost = defaults.imapHost ?: provider?.imapHost ?: "imap.$domain",
            imapPort = defaults.imapPort ?: provider?.imapPort ?: 993
        )
    }

    private data class Provider(
        val smtpHost: String,
        val smtpPort: Int,
        val imapHost: String,
        val imapPort: Int
    )

    private companion object {
        val providers = mapOf(
            "gmail.com" to Provider("smtp.gmail.com", 587, "imap.gmail.com", 993),
            "googlemail.com" to Provider("smtp.gmail.com", 587, "imap.gmail.com", 993),
            "yandex.ru" to Provider("smtp.yandex.ru", 587, "imap.yandex.ru", 993),
            "ya.ru" to Provider("smtp.yandex.ru", 587, "imap.yandex.ru", 993),
            "mail.ru" to Provider("smtp.mail.ru", 465, "imap.mail.ru", 993),
            "outlook.com" to Provider("smtp-mail.outlook.com", 587, "outlook.office365.com", 993),
            "hotmail.com" to Provider("smtp-mail.outlook.com", 587, "outlook.office365.com", 993)
        )
    }
}
