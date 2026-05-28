package com.example.workers.worker.mail

import com.example.domain.model.MailConnectionSettings
import com.example.domain.service.MailConnectionDefaults

class EmailProviderResolver {
    fun resolve(
        email: String,
        accessToken: String,
        smtpHost: String? = null,
        smtpPort: Int? = null,
        imapHost: String? = null,
        imapPort: Int? = null
    ): MailServerSettings {
        val resolved = MailConnectionDefaults.resolveForEmail(
            email = email,
            settings = MailConnectionSettings(
                token = accessToken,
                smtpHost = smtpHost,
                smtpPort = smtpPort,
                imapHost = imapHost,
                imapPort = imapPort
            )
        )

        return MailServerSettings(
            email = email,
            accessToken = accessToken,
            smtpHost = resolved.smtpHost ?: error("SMTP host is not configured for $email"),
            smtpPort = resolved.smtpPort ?: 587,
            imapHost = resolved.imapHost ?: error("IMAP host is not configured for $email"),
            imapPort = resolved.imapPort ?: 993
        )
    }
}
