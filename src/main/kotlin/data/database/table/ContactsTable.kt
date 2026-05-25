package com.example.data.database.table

import org.jetbrains.exposed.sql.Table

object ContactsTable : Table("contacts") {
    val id = long("id").autoIncrement()
    val profileId = long("profile_id").references(ProfilesTable.userId).index()
    val email = varchar("email", length = 255)
    val displayName = varchar("display_name", length = 128).nullable()
    val avatarUrl = text("avatar_url").nullable()
    val createdAt = varchar("created_at", length = 32)
    val updatedAt = varchar("updated_at", length = 32)

    init {
        uniqueIndex(profileId, email)
    }

    override val primaryKey = PrimaryKey(id)
}
