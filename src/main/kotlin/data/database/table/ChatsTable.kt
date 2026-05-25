package com.example.data.database.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ChatsTable : Table("chats") {
    val id = long("id").autoIncrement()
    val contactId = long("contact_id").references(ContactsTable.id, onDelete = ReferenceOption.CASCADE).index()
    val title = varchar("title", length = 128)
    val isDeleted = bool("is_deleted")
    val createdAt = varchar("created_at", length = 32)
    val updatedAt = varchar("updated_at", length = 32)

    override val primaryKey = PrimaryKey(id)
}
