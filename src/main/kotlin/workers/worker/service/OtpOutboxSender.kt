package com.example.workers.worker.service

import com.example.data.dao.OtpOutboxDao
import com.example.workers.worker.config.AuthMailConfig
import com.example.workers.worker.mail.MailServerSettings
import com.example.workers.worker.mail.OutgoingMail
import com.example.workers.worker.mail.SmtpMailClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

class OtpOutboxSender(
    private val otpOutboxDao: OtpOutboxDao,
    private val smtpMailClient: SmtpMailClient,
    private val authMailConfig: AuthMailConfig
) {
    suspend fun processPendingCodes(batchSize: Int) {
        authMailConfig.validate()
        ensureTables()

        val pending = dbQuery {
            otpOutboxDao.findPending(batchSize)
        }

        for (outbox in pending) {
            val code = outbox.code
            if (code == null) {
                dbQuery {
                    otpOutboxDao.markFailed(outbox.id, "OTP code is empty")
                }
                continue
            }

            try {
                smtpMailClient.send(
                    settings = MailServerSettings(
                        email = authMailConfig.username,
                        accessToken = authMailConfig.accessToken,
                        smtpHost = authMailConfig.smtpHost,
                        smtpPort = authMailConfig.smtpPort,
                        imapHost = authMailConfig.smtpHost,
                        imapPort = 993
                    ),
                    mail = OutgoingMail(
                        from = authMailConfig.fromEmail,
                        to = outbox.email,
                        subject = "Poster verification code",
                        bodyText = "Your Poster verification code: $code"
                    )
                )

                dbQuery {
                    otpOutboxDao.markSent(outbox.id)
                }
            } catch (exception: Exception) {
                dbQuery {
                    otpOutboxDao.markFailed(outbox.id, exception.message ?: exception::class.simpleName.orEmpty())
                }
            }
        }
    }

    private suspend fun ensureTables() = dbQuery {
        otpOutboxDao.ensureTable()
    }

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction {
                block()
            }
        }
    }
}
