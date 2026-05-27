package com.example.data.database.table

import org.jetbrains.exposed.sql.Table

object OtpOutboxTable : Table("otp_outbox") {
    val id = long("id").autoIncrement()
    val email = varchar("email", length = 255)
    val code = varchar("code", length = 6).nullable()
    val status = varchar("status", length = 16)
    val attempts = integer("attempts")
    val lastError = text("last_error").nullable()
    val createdAt = varchar("created_at", length = 32)
    val updatedAt = varchar("updated_at", length = 32)
    val sentAt = varchar("sent_at", length = 32).nullable()

    override val primaryKey = PrimaryKey(id)
}
