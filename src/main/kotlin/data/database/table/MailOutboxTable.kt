package com.example.data.database.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object MailOutboxTable : Table("mail_outbox") {
    val id = long("id").autoIncrement()
    val messageId = long("message_id")
        .references(MessagesTable.id, onDelete = ReferenceOption.CASCADE)
        .uniqueIndex()
    val status = varchar("status", length = 16)
    val attempts = integer("attempts")
    val lastError = text("last_error").nullable()
    val createdAt = varchar("created_at", length = 32)
    val updatedAt = varchar("updated_at", length = 32)
    val sentAt = varchar("sent_at", length = 32).nullable()

    override val primaryKey = PrimaryKey(id)
}
