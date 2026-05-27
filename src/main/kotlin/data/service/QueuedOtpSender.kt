package com.example.data.service

import com.example.data.dao.OtpOutboxDao
import com.example.domain.service.OtpSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

class QueuedOtpSender(
    private val otpOutboxDao: OtpOutboxDao
) : OtpSender {
    override suspend fun send(email: String, code: String) {
        withContext(Dispatchers.IO) {
            transaction {
                otpOutboxDao.ensureTable()
                otpOutboxDao.createPending(email, code)
            }
        }
    }
}
