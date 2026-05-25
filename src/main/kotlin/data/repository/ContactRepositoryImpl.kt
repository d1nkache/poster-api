package com.example.data.repository

import com.example.data.dao.ContactDao
import com.example.data.dao.ContactRecord
import com.example.data.dao.ProfileDao
import com.example.domain.model.ContactEntity
import com.example.domain.model.CreateContact
import com.example.domain.model.UpdateContact
import com.example.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

class ContactRepositoryImpl(
    private val contactDao: ContactDao,
    private val profileDao: ProfileDao
) : ContactRepository {
    override suspend fun getContacts(
        profileId: Long,
        query: String?
    ): List<ContactEntity> = dbQuery {
        ensureProfileAndContacts(profileId)

        contactDao.findByProfileId(profileId, query).map { it.toContactEntity() }
    }

    override suspend fun getContact(
        profileId: Long,
        contactId: Long
    ): ContactEntity? = dbQuery {
        ensureProfileAndContacts(profileId)

        contactDao.findById(profileId, contactId)?.toContactEntity()
    }

    override suspend fun createContact(
        profileId: Long,
        createContact: CreateContact
    ): ContactEntity = dbQuery {
        ensureProfileAndContacts(profileId)

        contactDao.create(profileId, createContact).toContactEntity()
    }

    override suspend fun updateContact(
        profileId: Long,
        contactId: Long,
        updateContact: UpdateContact
    ): ContactEntity? = dbQuery {
        ensureProfileAndContacts(profileId)

        contactDao.update(profileId, contactId, updateContact)?.toContactEntity()
    }

    override suspend fun deleteContact(
        profileId: Long,
        contactId: Long
    ): Boolean = dbQuery {
        ensureProfileAndContacts(profileId)

        contactDao.delete(profileId, contactId)
    }

    private fun ensureProfileAndContacts(profileId: Long) {
        profileDao.ensureTable()
        contactDao.ensureTable()

        if (profileDao.findByUserId(profileId) == null) {
            profileDao.createDefault(profileId)
        }
    }

    private fun ContactRecord.toContactEntity(): ContactEntity {
        return ContactEntity(
            id = id,
            profileId = profileId,
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction {
                block()
            }
        }
    }
}
