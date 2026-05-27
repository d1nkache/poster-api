package com.example.workers.worker.service

import com.example.data.dao.ChatDao
import com.example.data.dao.ContactDao
import com.example.data.dao.MessageDao
import com.example.data.dao.ProfileDao
import com.example.data.dao.ProfileRecord
import com.example.data.dao.SettingsDao
import com.example.domain.model.MailSyncStatus
import com.example.workers.worker.mail.EmailProviderResolver
import com.example.workers.worker.mail.ImapMailClient
import com.example.workers.worker.mail.IncomingMail
import com.example.workers.worker.mail.MailTokenCipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

class ImapSyncWorker(
    private val profileDao: ProfileDao,
    private val settingsDao: SettingsDao,
    private val contactDao: ContactDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val imapMailClient: ImapMailClient,
    private val providerResolver: EmailProviderResolver,
    private val tokenCipher: MailTokenCipher
) {
    suspend fun syncEnabledProfiles() {
        ensureTables()

        val profiles = dbQuery {
            profileDao.findMailSyncEnabled()
        }

        for (profile in profiles) {
            syncProfile(profile)
        }
    }

    private suspend fun syncProfile(profile: ProfileRecord) {
        val settings = dbQuery {
            settingsDao.findByUserId(profile.userId)
        } ?: return
        val token = settings.mailAccessToken ?: return

        try {
            dbQuery {
                profileDao.updateMailSyncStatus(profile.userId, MailSyncStatus.SYNCING)
            }

            val accessToken = tokenCipher.decrypt(token)
            val mailSettings = providerResolver.resolve(
                email = profile.email,
                accessToken = accessToken,
                smtpHost = settings.smtpHost,
                smtpPort = settings.smtpPort,
                imapHost = settings.imapHost,
                imapPort = settings.imapPort
            )
            val incomingMessages = imapMailClient.fetchNewMessages(mailSettings, profile.lastUid)
            val maxUid = incomingMessages.mapNotNull { it.uid }.maxOrNull()

            dbQuery {
                incomingMessages.forEach { incoming ->
                    persistIncomingMessage(profile, incoming)
                }
                profileDao.updateMailSyncState(
                    userId = profile.userId,
                    lastUid = maxUid,
                    syncStatus = MailSyncStatus.IDLE
                )
            }
        } catch (_: Exception) {
            dbQuery {
                profileDao.updateMailSyncStatus(profile.userId, MailSyncStatus.FAILED)
            }
        }
    }

    private fun persistIncomingMessage(profile: ProfileRecord, incoming: IncomingMail) {
        val contact = contactDao.findOrCreateByEmail(
            profileId = profile.userId,
            email = incoming.fromEmail,
            displayName = incoming.fromDisplayName
        )
        val chat = chatDao.findByContactId(contact.id) ?: chatDao.create(contact)

        if (messageDao.existsIncoming(chat.id, incoming.uid, incoming.providerMessageId)) {
            return
        }

        messageDao.createIncoming(
            chatId = chat.id,
            bodyText = incoming.bodyText,
            imapUid = incoming.uid,
            providerMessageId = incoming.providerMessageId,
            receivedAt = incoming.receivedAt
        )
    }

    private suspend fun ensureTables() = dbQuery {
        profileDao.ensureTable()
        settingsDao.ensureTable()
        contactDao.ensureTable()
        chatDao.ensureTable()
        messageDao.ensureTable()
    }

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction {
                block()
            }
        }
    }
}
