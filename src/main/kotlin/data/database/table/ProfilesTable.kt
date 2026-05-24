package com.example.data.database.table

import org.jetbrains.exposed.sql.Table

object ProfilesTable : Table("profiles") {
    val userId = long("user_id")
    val username = varchar("username", length = 32).uniqueIndex()
    val bio = text("bio").nullable()

    override val primaryKey = PrimaryKey(userId)
}
