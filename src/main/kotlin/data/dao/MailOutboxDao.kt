package com.example.data.dao

import com.example.data.database.table.ChatsTable
import com.example.data.database.table.ContactsTable
import com.example.data.database.table.MailOutboxTable
import com.example.data.database.table.MessagesTable
import com.example.data.database.table.ProfilesTable
import com.example.data.database.table.SettingsTable
import com.example.domain.model.MailOutboxStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class MailOutboxDao {
    fun ensureTable() {
        SchemaUtils.createMissingTablesAndColumns(MailOutboxTable)
    }

    fun createPending(messageId: Long): MailOutboxRecord {
        val now = Instant.now().toString()
        val outboxId = MailOutboxTable.insert { statement ->
            statement[MailOutboxTable.messageId] = messageId
            statement[status] = MailOutboxStatus.PENDING.name
            statement[attempts] = 0
            statement[lastError] = null
            statement[createdAt] = now
            statement[updatedAt] = now
            statement[sentAt] = null
        } get MailOutboxTable.id

        return findById(outboxId) ?: error("Created mail_outbox row was not found")
    }

    fun findPending(limit: Int): List<MailOutboxRecord> {
        return MailOutboxTable
            .selectAll()
            .where { MailOutboxTable.status eq MailOutboxStatus.PENDING.name }
            .orderBy(MailOutboxTable.createdAt to SortOrder.ASC)
            .limit(limit)
            .map { it.toMailOutboxRecord() }
    }

    fun findPendingDetails(limit: Int): List<PendingOutgoingMailRecord> {
        return MailOutboxTable
            .innerJoin(MessagesTable)
            .innerJoin(ChatsTable)
            .innerJoin(ContactsTable)
            .innerJoin(ProfilesTable)
            .innerJoin(SettingsTable)
            .selectAll()
            .where {
                MailOutboxTable.status eq MailOutboxStatus.PENDING.name
            }
            .orderBy(MailOutboxTable.createdAt to SortOrder.ASC)
            .limit(limit)
            .filter { it[SettingsTable.mailAccessToken] != null }
            .map {
                PendingOutgoingMailRecord(
                    outboxId = it[MailOutboxTable.id],
                    messageId = it[MessagesTable.id],
                    fromEmail = it[ProfilesTable.email],
                    toEmail = it[ContactsTable.email],
                    bodyText = it[MessagesTable.bodyText],
                    mailAccessToken = requireNotNull(it[SettingsTable.mailAccessToken])
                )
            }
    }

    fun markSent(id: Long): Boolean {
        val now = Instant.now().toString()
        return MailOutboxTable.update({ MailOutboxTable.id eq id }) { statement ->
            statement[status] = MailOutboxStatus.SENT.name
            statement[updatedAt] = now
            statement[sentAt] = now
            statement[lastError] = null
        } > 0
    }

    fun markFailed(id: Long, error: String): Boolean {
        val outbox = findById(id) ?: return false
        val now = Instant.now().toString()
        return MailOutboxTable.update({ MailOutboxTable.id eq id }) { statement ->
            statement[status] = MailOutboxStatus.FAILED.name
            statement[attempts] = outbox.attempts + 1
            statement[lastError] = error
            statement[updatedAt] = now
        } > 0
    }

    private fun findById(id: Long): MailOutboxRecord? {
        return MailOutboxTable
            .selectAll()
            .where { MailOutboxTable.id eq id }
            .singleOrNull()
            ?.toMailOutboxRecord()
    }

    private fun ResultRow.toMailOutboxRecord(): MailOutboxRecord {
        return MailOutboxRecord(
            id = this[MailOutboxTable.id],
            messageId = this[MailOutboxTable.messageId],
            status = MailOutboxStatus.valueOf(this[MailOutboxTable.status]),
            attempts = this[MailOutboxTable.attempts],
            lastError = this[MailOutboxTable.lastError],
            createdAt = this[MailOutboxTable.createdAt],
            updatedAt = this[MailOutboxTable.updatedAt],
            sentAt = this[MailOutboxTable.sentAt]
        )
    }
}

data class MailOutboxRecord(
    val id: Long,
    val messageId: Long,
    val status: MailOutboxStatus,
    val attempts: Int,
    val lastError: String?,
    val createdAt: String,
    val updatedAt: String,
    val sentAt: String?
)

data class PendingOutgoingMailRecord(
    val outboxId: Long,
    val messageId: Long,
    val fromEmail: String,
    val toEmail: String,
    val bodyText: String,
    val mailAccessToken: String
)
