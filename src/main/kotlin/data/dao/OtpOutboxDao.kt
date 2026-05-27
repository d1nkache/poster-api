package com.example.data.dao

import com.example.data.database.table.OtpOutboxTable
import com.example.domain.model.MailOutboxStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

class OtpOutboxDao {
    fun ensureTable() {
        SchemaUtils.createMissingTablesAndColumns(OtpOutboxTable)
    }

    fun createPending(email: String, code: String): OtpOutboxRecord {
        val now = Instant.now().toString()
        val outboxId = OtpOutboxTable.insert { statement ->
            statement[OtpOutboxTable.email] = email.lowercase()
            statement[OtpOutboxTable.code] = code
            statement[status] = MailOutboxStatus.PENDING.name
            statement[attempts] = 0
            statement[lastError] = null
            statement[createdAt] = now
            statement[updatedAt] = now
            statement[sentAt] = null
        } get OtpOutboxTable.id

        return findById(outboxId) ?: error("Created otp_outbox row was not found")
    }

    fun findPending(limit: Int): List<OtpOutboxRecord> {
        return OtpOutboxTable
            .selectAll()
            .where { OtpOutboxTable.status eq MailOutboxStatus.PENDING.name }
            .orderBy(OtpOutboxTable.createdAt to SortOrder.ASC)
            .limit(limit)
            .map { it.toOtpOutboxRecord() }
    }

    fun markSent(id: Long): Boolean {
        val now = Instant.now().toString()
        return OtpOutboxTable.update({ OtpOutboxTable.id eq id }) { statement ->
            statement[status] = MailOutboxStatus.SENT.name
            statement[code] = null
            statement[updatedAt] = now
            statement[sentAt] = now
            statement[lastError] = null
        } > 0
    }

    fun markFailed(id: Long, error: String): Boolean {
        val outbox = findById(id) ?: return false
        val now = Instant.now().toString()
        return OtpOutboxTable.update({ OtpOutboxTable.id eq id }) { statement ->
            statement[status] = MailOutboxStatus.FAILED.name
            statement[attempts] = outbox.attempts + 1
            statement[lastError] = error
            statement[updatedAt] = now
        } > 0
    }

    private fun findById(id: Long): OtpOutboxRecord? {
        return OtpOutboxTable
            .selectAll()
            .where { OtpOutboxTable.id eq id }
            .singleOrNull()
            ?.toOtpOutboxRecord()
    }

    private fun ResultRow.toOtpOutboxRecord(): OtpOutboxRecord {
        return OtpOutboxRecord(
            id = this[OtpOutboxTable.id],
            email = this[OtpOutboxTable.email],
            code = this[OtpOutboxTable.code],
            status = MailOutboxStatus.valueOf(this[OtpOutboxTable.status]),
            attempts = this[OtpOutboxTable.attempts],
            lastError = this[OtpOutboxTable.lastError],
            createdAt = this[OtpOutboxTable.createdAt],
            updatedAt = this[OtpOutboxTable.updatedAt],
            sentAt = this[OtpOutboxTable.sentAt]
        )
    }
}

data class OtpOutboxRecord(
    val id: Long,
    val email: String,
    val code: String?,
    val status: MailOutboxStatus,
    val attempts: Int,
    val lastError: String?,
    val createdAt: String,
    val updatedAt: String,
    val sentAt: String?
)
