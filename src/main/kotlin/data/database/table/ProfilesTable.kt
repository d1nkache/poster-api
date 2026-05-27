package com.example.data.database.table

import org.jetbrains.exposed.sql.Table

object ProfilesTable : Table("profiles") {
    val userId = long("user_id")
    val name = varchar("name", length = 128)
    val username = varchar("username", length = 32).uniqueIndex()
    val email = varchar("email", length = 255).uniqueIndex()
    val passwordHash = text("password_hash").nullable()
    val birthday = varchar("birthday", length = 10).nullable()
    val bio = text("bio").nullable()
    val isOnline = bool("is_online")
    val isVerified = bool("is_verified").default(false)
    val otpCodeHash = text("otp_code_hash").nullable()
    val otpExpiresAt = varchar("otp_expires_at", length = 32).nullable()
    val refreshTokenHash = text("refresh_token_hash").nullable()
    val createdAt = varchar("created_at", length = 32).nullable()
    val updatedAt = varchar("updated_at", length = 32).nullable()
    val mailSyncEnabled = bool("mail_sync_enabled").default(false)
    val lastUid = long("last_uid").nullable()
    val lastSyncAt = varchar("last_sync_at", length = 32).nullable()
    val syncStatus = varchar("sync_status", length = 16).default("IDLE")

    override val primaryKey = PrimaryKey(userId)
}
