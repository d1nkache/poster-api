package com.example.data.database.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object MessagesTable : Table("messages") {
    val id = long("id").autoIncrement()
    val chatId = long("chat_id").references(ChatsTable.id, onDelete = ReferenceOption.CASCADE).index()
    val bodyText = text("body_text")
    val direction = varchar("direction", length = 16)
    val status = varchar("status", length = 16)
    val isRead = bool("is_read")
    val isDeleted = bool("is_deleted")
    val imapUid = long("imap_uid").nullable()
    val providerMessageId = varchar("provider_message_id", length = 255).nullable()
    val createdAt = varchar("created_at", length = 32)
    val sentAt = varchar("sent_at", length = 32).nullable()
    val receivedAt = varchar("received_at", length = 32).nullable()

    override val primaryKey = PrimaryKey(id)
}
