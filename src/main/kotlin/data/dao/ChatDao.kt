package com.example.data.dao

import com.example.data.database.table.ChatsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ChatDao {
    fun ensureTable() {
        SchemaUtils.createMissingTablesAndColumns(ChatsTable)
    }

    fun findByContactId(contactId: Long): ChatRecord? {
        return ChatsTable
            .selectAll()
            .where { ChatsTable.contactId eq contactId }
            .singleOrNull()
            ?.toChatRecord()
    }

    fun create(contact: ContactRecord): ChatRecord {
        val now = Instant.now().toString()
        val chatId = ChatsTable.insert { statement ->
            statement[contactId] = contact.id
            statement[title] = contact.displayName ?: contact.email
            statement[isDeleted] = false
            statement[createdAt] = now
            statement[updatedAt] = now
        } get ChatsTable.id

        return requireNotNull(findById(chatId))
    }

    private fun findById(chatId: Long): ChatRecord? {
        return ChatsTable
            .selectAll()
            .where { ChatsTable.id eq chatId }
            .singleOrNull()
            ?.toChatRecord()
    }

    private fun ResultRow.toChatRecord(): ChatRecord {
        return ChatRecord(
            id = this[ChatsTable.id],
            contactId = this[ChatsTable.contactId],
            title = this[ChatsTable.title],
            isDeleted = this[ChatsTable.isDeleted],
            createdAt = this[ChatsTable.createdAt],
            updatedAt = this[ChatsTable.updatedAt]
        )
    }
}

data class ChatRecord(
    val id: Long,
    val contactId: Long,
    val title: String,
    val isDeleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)
