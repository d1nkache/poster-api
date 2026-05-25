package com.example.data.database.table

import org.jetbrains.exposed.sql.Table

object SettingsTable : Table("settings") {
    val userId = long("user_id").references(ProfilesTable.userId)
    val language = varchar("language", length = 16)
    val mailAccessToken = text("mail_access_token").nullable()

    override val primaryKey = PrimaryKey(userId)
}
