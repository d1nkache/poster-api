package com.example.data.database.table

import org.jetbrains.exposed.sql.Table

object SettingsTable : Table("settings") {
    val userId = long("user_id").references(ProfilesTable.userId)
    val language = varchar("language", length = 16)
    val mailAccessToken = text("mail_access_token").nullable()
    val smtpHost = varchar("smtp_host", length = 255).nullable()
    val smtpPort = integer("smtp_port").nullable()
    val imapHost = varchar("imap_host", length = 255).nullable()
    val imapPort = integer("imap_port").nullable()

    override val primaryKey = PrimaryKey(userId)
}
