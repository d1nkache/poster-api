package com.example.workers.worker.service

import com.example.data.dao.ChatDao
import com.example.data.dao.ContactDao
import com.example.data.dao.MailOutboxDao
import com.example.data.dao.MessageDao
import com.example.data.dao.ProfileDao
import com.example.data.dao.SettingsDao
import com.example.workers.worker.mail.EmailProviderResolver
import com.example.workers.worker.mail.MailTokenCipher
import com.example.workers.worker.mail.OutgoingMail
import com.example.workers.worker.mail.SmtpMailClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

class OutboxSender(
    private val profileDao: ProfileDao,
    private val settingsDao: SettingsDao,
    private val contactDao: ContactDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val mailOutboxDao: MailOutboxDao,
    private val smtpMailClient: SmtpMailClient,
    private val providerResolver: EmailProviderResolver,
    private val tokenCipher: MailTokenCipher
) {
    suspend fun processPendingMessages(batchSize: Int) {
        ensureTables()

        val pending = dbQuery {
            mailOutboxDao.findPendingDetails(batchSize)
        }

        for (outbox in pending) {
            try {
                val accessToken = tokenCipher.decrypt(outbox.mailAccessToken)
                val mailSettings = providerResolver.resolve(
                    email = outbox.fromEmail,
                    accessToken = accessToken,
                    smtpHost = outbox.smtpHost,
                    smtpPort = outbox.smtpPort,
                    imapHost = outbox.imapHost,
                    imapPort = outbox.imapPort
                )
                smtpMailClient.send(
                    settings = mailSettings,
                    mail = OutgoingMail(
                        from = outbox.fromEmail,
                        to = outbox.toEmail,
                        subject = "Poster message",
                        bodyText = outbox.bodyText
                    )
                )

                dbQuery {
                    messageDao.markSentForWorker(outbox.messageId)
                    mailOutboxDao.markSent(outbox.outboxId)
                }
            } catch (exception: Exception) {
                dbQuery {
                    messageDao.markFailedForWorker(outbox.messageId)
                    mailOutboxDao.markFailed(outbox.outboxId, exception.message ?: exception::class.simpleName.orEmpty())
                }
            }
        }
    }

    private suspend fun ensureTables() = dbQuery {
        profileDao.ensureTable()
        settingsDao.ensureTable()
        contactDao.ensureTable()
        chatDao.ensureTable()
        messageDao.ensureTable()
        mailOutboxDao.ensureTable()
    }

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction {
                block()
            }
        }
    }
}
