package com.example.data.database.table

import org.jetbrains.exposed.sql.Table

object ProfilesTable : Table("profiles") {
    val userId = long("user_id")
    val name = varchar("name", length = 128)
    val username = varchar("username", length = 32).uniqueIndex()
    val email = varchar("email", length = 255).uniqueIndex()
    val birthday = varchar("birthday", length = 10).nullable()
    val bio = text("bio").nullable()
    val isOnline = bool("is_online")
    val mailSyncEnabled = bool("mail_sync_enabled").default(false)
    val lastUid = long("last_uid").nullable()
    val lastSyncAt = varchar("last_sync_at", length = 32).nullable()
    val syncStatus = varchar("sync_status", length = 16).default("IDLE")

    override val primaryKey = PrimaryKey(userId)
}
