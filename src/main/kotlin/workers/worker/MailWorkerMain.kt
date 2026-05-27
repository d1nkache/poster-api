package com.example.workers.worker

import com.example.data.dao.ChatDao
import com.example.data.dao.ContactDao
import com.example.data.dao.MailOutboxDao
import com.example.data.dao.MessageDao
import com.example.data.dao.ProfileDao
import com.example.data.dao.SettingsDao
import com.example.workers.worker.config.WorkerConfig
import com.example.workers.worker.config.WorkerDatabase
import com.example.workers.worker.mail.EmailProviderResolver
import com.example.workers.worker.mail.JakartaImapMailClient
import com.example.workers.worker.mail.JakartaSmtpMailClient
import com.example.workers.worker.mail.PlainTextMailTokenCipher
import com.example.workers.worker.service.ImapSyncWorker
import com.example.workers.worker.service.OutboxSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val config = WorkerConfig.fromEnvironment()
        WorkerDatabase.connect(config.database)

        val profileDao = ProfileDao()
        val settingsDao = SettingsDao()
        val contactDao = ContactDao()
        val chatDao = ChatDao()
        val messageDao = MessageDao()
        val mailOutboxDao = MailOutboxDao()
        val providerResolver = EmailProviderResolver(config.mail)
        val tokenCipher = PlainTextMailTokenCipher()

        val outboxSender = OutboxSender(
            profileDao = profileDao,
            settingsDao = settingsDao,
            contactDao = contactDao,
            chatDao = chatDao,
            messageDao = messageDao,
            mailOutboxDao = mailOutboxDao,
            smtpMailClient = JakartaSmtpMailClient(),
            providerResolver = providerResolver,
            tokenCipher = tokenCipher
        )
        val imapSyncWorker = ImapSyncWorker(
            profileDao = profileDao,
            settingsDao = settingsDao,
            contactDao = contactDao,
            chatDao = chatDao,
            messageDao = messageDao,
            imapMailClient = JakartaImapMailClient(),
            providerResolver = providerResolver,
            tokenCipher = tokenCipher
        )

        val workerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        Runtime.getRuntime().addShutdownHook(
            Thread {
                workerScope.cancel()
                WorkerDatabase.close()
            }
        )

        workerScope.launch {
            while (isActive) {
                outboxSender.processPendingMessages(config.outboxBatchSize)
                delay(config.outboxDelayMs)
            }
        }

        workerScope.launch {
            while (isActive) {
                imapSyncWorker.syncEnabledProfiles()
                delay(config.imapDelayMs)
            }
        }

        awaitCancellation()
    }
}
