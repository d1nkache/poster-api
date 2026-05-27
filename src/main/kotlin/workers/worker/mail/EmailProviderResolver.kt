package com.example.workers.worker.mail

import com.example.workers.worker.config.MailDefaults
import com.example.domain.model.MailConnectionSettings
import com.example.domain.service.MailConnectionDefaults

class EmailProviderResolver(
    private val defaults: MailDefaults
) {
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
            smtpHost = resolved.smtpHost ?: defaults.smtpHost ?: error("SMTP host is not configured"),
            smtpPort = resolved.smtpPort ?: defaults.smtpPort ?: 587,
            imapHost = resolved.imapHost ?: defaults.imapHost ?: error("IMAP host is not configured"),
            imapPort = resolved.imapPort ?: defaults.imapPort ?: 993
        )
    }
}
