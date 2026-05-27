package com.example.workers.worker.mail

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties

interface SmtpMailClient {
    suspend fun send(settings: MailServerSettings, mail: OutgoingMail)
}

class JakartaSmtpMailClient : SmtpMailClient {
    override suspend fun send(settings: MailServerSettings, mail: OutgoingMail) {
        withContext(Dispatchers.IO) {
            val session = Session.getInstance(
                smtpProperties(settings),
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(settings.email, settings.accessToken)
                    }
                }
            )

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(mail.from))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.to))
                subject = mail.subject
                setText(mail.bodyText, Charsets.UTF_8.name())
            }

            Transport.send(message)
        }
    }

    private fun smtpProperties(settings: MailServerSettings): Properties {
        return Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", settings.smtpHost)
            put("mail.smtp.port", settings.smtpPort.toString())
            put("mail.smtp.starttls.enable", (settings.smtpPort != 465).toString())
            put("mail.smtp.ssl.enable", (settings.smtpPort == 465).toString())
        }
    }
}
