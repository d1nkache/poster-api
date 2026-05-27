package com.example.data.repository

import com.example.data.dao.ChatDao
import com.example.data.dao.ContactDao
import com.example.data.dao.MailOutboxDao
import com.example.data.dao.MessageDao
import com.example.data.dao.MessageRecord
import com.example.domain.model.MessageEntity
import com.example.domain.model.SendMessage
import com.example.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

class MessageRepositoryImpl(
    private val messageDao: MessageDao,
    private val mailOutboxDao: MailOutboxDao,
    private val chatDao: ChatDao,
    private val contactDao: ContactDao
) : MessageRepository {
    override suspend fun getChatMessages(
        profileId: Long,
        chatId: Long,
        limit: Int,
        offset: Long
    ): List<MessageEntity>? = dbQuery {
        ensureTables()

        messageDao.findByChatId(profileId, chatId, limit, offset)?.map { it.toMessageEntity() }
    }

    override suspend fun sendMessage(
        profileId: Long,
        chatId: Long,
        sendMessage: SendMessage
    ): MessageEntity? = dbQuery {
        ensureTables()

        val message = messageDao.createOutgoing(profileId, chatId, sendMessage) ?: return@dbQuery null
        mailOutboxDao.createPending(message.id)

        message.toMessageEntity()
    }

    override suspend fun getMessage(
        profileId: Long,
        messageId: Long
    ): MessageEntity? = dbQuery {
        ensureTables()

        messageDao.findById(profileId, messageId)?.toMessageEntity()
    }

    override suspend fun markMessageAsRead(
        profileId: Long,
        messageId: Long
    ): Boolean = dbQuery {
        ensureTables()

        messageDao.markMessageAsRead(profileId, messageId)
    }

    override suspend fun markChatMessagesAsRead(
        profileId: Long,
        chatId: Long
    ): Boolean = dbQuery {
        ensureTables()

        messageDao.markChatMessagesAsRead(profileId, chatId)
    }

    override suspend fun deleteMessage(
        profileId: Long,
        messageId: Long
    ): Boolean = dbQuery {
        ensureTables()

        messageDao.softDelete(profileId, messageId)
    }

    private fun ensureTables() {
        contactDao.ensureTable()
        chatDao.ensureTable()
        messageDao.ensureTable()
        mailOutboxDao.ensureTable()
    }

    private fun MessageRecord.toMessageEntity(): MessageEntity {
        return MessageEntity(
            id = id,
            chatId = chatId,
            bodyText = bodyText,
            direction = direction,
            status = status,
            isRead = isRead,
            createdAt = createdAt,
            sentAt = sentAt,
            receivedAt = receivedAt
        )
    }

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction {
                block()
            }
        }
    }
}
