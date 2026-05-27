package com.example.data.dao

import com.example.data.database.table.ChatsTable
import com.example.data.database.table.ContactsTable
import com.example.data.database.table.MessagesTable
import com.example.domain.model.MessageDirection
import com.example.domain.model.MessageStatus
import com.example.domain.model.SendMessage
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class MessageDao {
    fun ensureTable() {
        SchemaUtils.createMissingTablesAndColumns(MessagesTable)
    }

    fun chatBelongsToProfile(
        profileId: Long,
        chatId: Long
    ): Boolean {
        return ContactsTable
            .innerJoin(ChatsTable)
            .selectAll()
            .where { (ChatsTable.id eq chatId) and (ContactsTable.profileId eq profileId) }
            .limit(1)
            .any()
    }

    fun findByChatId(
        profileId: Long,
        chatId: Long,
        limit: Int,
        offset: Long
    ): List<MessageRecord>? {
        if (!chatBelongsToProfile(profileId, chatId)) {
            return null
        }

        return MessagesTable
            .selectAll()
            .where { (MessagesTable.chatId eq chatId) and (MessagesTable.isDeleted eq false) }
            .orderBy(MessagesTable.createdAt to SortOrder.ASC)
            .limit(limit, offset)
            .map { it.toMessageRecord() }
    }

    fun createOutgoing(
        profileId: Long,
        chatId: Long,
        sendMessage: SendMessage
    ): MessageRecord? {
        if (!chatBelongsToProfile(profileId, chatId)) {
            return null
        }

        val now = Instant.now().toString()
        val messageId = MessagesTable.insert { statement ->
            statement[MessagesTable.chatId] = chatId
            statement[bodyText] = sendMessage.bodyText
            statement[direction] = MessageDirection.OUTGOING.name
            statement[status] = MessageStatus.PENDING.name
            statement[isRead] = true
            statement[isDeleted] = false
            statement[imapUid] = null
            statement[providerMessageId] = null
            statement[createdAt] = now
            statement[sentAt] = null
            statement[receivedAt] = null
        } get MessagesTable.id

        return findById(profileId, messageId)
    }

    fun findById(
        profileId: Long,
        messageId: Long
    ): MessageRecord? {
        return MessagesTable
            .innerJoin(ChatsTable)
            .innerJoin(ContactsTable)
            .selectAll()
            .where {
                (MessagesTable.id eq messageId) and
                    (ContactsTable.profileId eq profileId) and
                    (MessagesTable.isDeleted eq false)
            }
            .singleOrNull()
            ?.toMessageRecord()
    }

    fun markMessageAsRead(
        profileId: Long,
        messageId: Long
    ): Boolean {
        val message = findById(profileId, messageId) ?: return false

        return MessagesTable.update({ MessagesTable.id eq message.id }) { statement ->
            statement[isRead] = true
        } > 0
    }

    fun markChatMessagesAsRead(
        profileId: Long,
        chatId: Long
    ): Boolean {
        if (!chatBelongsToProfile(profileId, chatId)) {
            return false
        }

        MessagesTable.update({
            (MessagesTable.chatId eq chatId) and
                (MessagesTable.direction eq MessageDirection.INCOMING.name) and
                (MessagesTable.isDeleted eq false)
        }) { statement ->
            statement[isRead] = true
        }

        return true
    }

    fun softDelete(
        profileId: Long,
        messageId: Long
    ): Boolean {
        val message = findById(profileId, messageId) ?: return false

        return MessagesTable.update({ MessagesTable.id eq message.id }) { statement ->
            statement[isDeleted] = true
        } > 0
    }

    private fun ResultRow.toMessageRecord(): MessageRecord {
        return MessageRecord(
            id = this[MessagesTable.id],
            chatId = this[MessagesTable.chatId],
            bodyText = this[MessagesTable.bodyText],
            direction = MessageDirection.valueOf(this[MessagesTable.direction]),
            status = MessageStatus.valueOf(this[MessagesTable.status]),
            isRead = this[MessagesTable.isRead],
            createdAt = this[MessagesTable.createdAt],
            sentAt = this[MessagesTable.sentAt],
            receivedAt = this[MessagesTable.receivedAt]
        )
    }
}

data class MessageRecord(
    val id: Long,
    val chatId: Long,
    val bodyText: String,
    val direction: MessageDirection,
    val status: MessageStatus,
    val isRead: Boolean,
    val createdAt: String,
    val sentAt: String?,
    val receivedAt: String?
)
