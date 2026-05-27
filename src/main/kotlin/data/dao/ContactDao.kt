package com.example.data.dao

import com.example.data.database.table.ContactsTable
import com.example.domain.model.CreateContact
import com.example.domain.model.UpdateContact
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ContactDao {
    fun ensureTable() {
        SchemaUtils.createMissingTablesAndColumns(ContactsTable)
    }

    fun findByProfileId(
        profileId: Long,
        query: String?
    ): List<ContactRecord> {
        val normalizedQuery = query?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

        return ContactsTable
            .selectAll()
            .where { ContactsTable.profileId eq profileId }
            .orderBy(ContactsTable.displayName to SortOrder.ASC, ContactsTable.email to SortOrder.ASC)
            .map { it.toContactRecord() }
            .filter { contact ->
                normalizedQuery == null ||
                    contact.email.lowercase().contains(normalizedQuery) ||
                    contact.displayName?.lowercase()?.contains(normalizedQuery) == true
            }
    }

    fun findById(
        profileId: Long,
        contactId: Long
    ): ContactRecord? {
        return ContactsTable
            .selectAll()
            .where { (ContactsTable.id eq contactId) and (ContactsTable.profileId eq profileId) }
            .singleOrNull()
            ?.toContactRecord()
    }

    fun findByEmail(profileId: Long, email: String): ContactRecord? {
        return ContactsTable
            .selectAll()
            .where { (ContactsTable.profileId eq profileId) and (ContactsTable.email eq email) }
            .singleOrNull()
            ?.toContactRecord()
    }

    fun findOrCreateByEmail(
        profileId: Long,
        email: String,
        displayName: String?
    ): ContactRecord {
        findByEmail(profileId, email)?.let { return it }

        return create(
            profileId = profileId,
            createContact = CreateContact(
                email = email,
                displayName = displayName
            )
        )
    }

    fun create(
        profileId: Long,
        createContact: CreateContact
    ): ContactRecord {
        val now = now()
        val contactId = ContactsTable.insert { statement ->
            statement[ContactsTable.profileId] = profileId
            statement[email] = createContact.email
            statement[displayName] = createContact.displayName
            statement[avatarUrl] = null
            statement[createdAt] = now
            statement[updatedAt] = now
        } get ContactsTable.id

        return requireNotNull(findById(profileId, contactId))
    }

    fun update(
        profileId: Long,
        contactId: Long,
        updateContact: UpdateContact
    ): ContactRecord? {
        val updatedRows = ContactsTable.update({
            (ContactsTable.id eq contactId) and (ContactsTable.profileId eq profileId)
        }) { statement ->
            updateContact.displayName?.let { statement[displayName] = it }
            updateContact.avatarUrl?.let { statement[avatarUrl] = it }
            statement[updatedAt] = now()
        }

        if (updatedRows == 0) {
            return null
        }

        return findById(profileId, contactId)
    }

    fun delete(
        profileId: Long,
        contactId: Long
    ): Boolean {
        return ContactsTable.deleteWhere {
            (ContactsTable.id eq contactId) and (ContactsTable.profileId eq profileId)
        } > 0
    }

    private fun ResultRow.toContactRecord(): ContactRecord {
        return ContactRecord(
            id = this[ContactsTable.id],
            profileId = this[ContactsTable.profileId],
            email = this[ContactsTable.email],
            displayName = this[ContactsTable.displayName],
            avatarUrl = this[ContactsTable.avatarUrl],
            createdAt = this[ContactsTable.createdAt],
            updatedAt = this[ContactsTable.updatedAt]
        )
    }

    private fun now(): String {
        return Instant.now().toString()
    }
}

data class ContactRecord(
    val id: Long,
    val profileId: Long,
    val email: String,
    val displayName: String?,
    val avatarUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
