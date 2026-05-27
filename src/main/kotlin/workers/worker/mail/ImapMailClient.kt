package com.example.workers.worker.mail

import jakarta.mail.Address
import jakarta.mail.Flags
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.Multipart
import jakarta.mail.Part
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import jakarta.mail.search.FlagTerm
import jakarta.mail.UIDFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Properties

interface ImapMailClient {
    suspend fun fetchNewMessages(
        settings: MailServerSettings,
        lastUid: Long?
    ): List<IncomingMail>
}

class JakartaImapMailClient : ImapMailClient {
    override suspend fun fetchNewMessages(
        settings: MailServerSettings,
        lastUid: Long?
    ): List<IncomingMail> {
        return withContext(Dispatchers.IO) {
            val session = Session.getInstance(imapProperties(settings))
            val store = session.getStore("imaps")

            store.use {
                it.connect(settings.imapHost, settings.imapPort, settings.email, settings.accessToken)
                val folder = it.getFolder("INBOX")
                folder.open(Folder.READ_ONLY)

                try {
                    val uidFolder = folder as? UIDFolder
                    val messages = if (uidFolder != null && lastUid != null) {
                        uidFolder.getMessagesByUID(lastUid + 1, UIDFolder.LASTUID)
                            .filterNotNull()
                            .toTypedArray()
                    } else {
                        folder.search(FlagTerm(Flags(Flags.Flag.SEEN), false))
                    }

                    messages.mapNotNull { message ->
                        val uid = uidFolder?.getUID(message)?.takeIf { uid -> uid > 0 }
                        if (lastUid != null && uid != null && uid <= lastUid) {
                            return@mapNotNull null
                        }

                        message.toIncomingMail(uid)
                    }
                } finally {
                    folder.close(false)
                }
            }
        }
    }

    private fun Message.toIncomingMail(uid: Long?): IncomingMail {
        val from = from.firstOrNull().asInternetAddress()
        val providerMessageId = (this as? MimeMessage)?.messageID

        return IncomingMail(
            uid = uid,
            providerMessageId = providerMessageId,
            fromEmail = from?.address ?: "unknown@example.com",
            fromDisplayName = from?.personal,
            subject = subject,
            bodyText = extractBodyText(this).orEmpty(),
            receivedAt = receivedDate?.toInstant()?.toString() ?: Instant.now().toString()
        )
    }

    private fun Address?.asInternetAddress(): InternetAddress? {
        return this as? InternetAddress
    }

    private fun extractBodyText(part: Part): String? {
        if (part.isMimeType("text/plain")) {
            return part.content as? String
        }

        if (part.isMimeType("multipart/*")) {
            val multipart = part.content as? Multipart ?: return null
            for (index in 0 until multipart.count) {
                val text = extractBodyText(multipart.getBodyPart(index))
                if (!text.isNullOrBlank()) {
                    return text
                }
            }
        }

        return null
    }

    private fun imapProperties(settings: MailServerSettings): Properties {
        return Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.imaps.host", settings.imapHost)
            put("mail.imaps.port", settings.imapPort.toString())
            put("mail.imaps.ssl.enable", "true")
        }
    }
}
