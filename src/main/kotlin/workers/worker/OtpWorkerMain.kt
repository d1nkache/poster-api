package com.example.workers.worker

import com.example.data.dao.OtpOutboxDao
import com.example.workers.worker.config.WorkerConfig
import com.example.workers.worker.config.WorkerDatabase
import com.example.workers.worker.mail.JakartaSmtpMailClient
import com.example.workers.worker.service.OtpOutboxSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val config = WorkerConfig.fromEnvironment()
        config.authMail.validate()
        WorkerDatabase.connect(config.database)

        val otpOutboxSender = OtpOutboxSender(
            otpOutboxDao = OtpOutboxDao(),
            smtpMailClient = JakartaSmtpMailClient(),
            authMailConfig = config.authMail
        )

        Runtime.getRuntime().addShutdownHook(
            Thread {
                WorkerDatabase.close()
            }
        )

        while (true) {
            otpOutboxSender.processPendingCodes(config.otpBatchSize)
            delay(config.otpDelayMs)
        }
    }
}
