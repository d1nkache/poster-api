package com.example.data.repository

import com.example.data.dao.ChatDao
import com.example.data.dao.ChatRecord
import com.example.data.dao.ContactDao
import com.example.data.dao.ContactRecord
import com.example.data.dao.ProfileDao
import com.example.domain.model.ChatEntity
import com.example.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

class ChatRepositoryImpl(
    private val chatDao: ChatDao,
    private val contactDao: ContactDao,
    private val profileDao: ProfileDao
) : ChatRepository {
    override suspend fun getOrCreateChatByContact(
        profileId: Long,
        contactId: Long
    ): ChatEntity? = dbQuery {
        ensureTables(profileId)
        val contact = contactDao.findById(profileId, contactId) ?: return@dbQuery null
        val chat = chatDao.findByContactId(contactId) ?: chatDao.create(contact)

        chat.toChatEntity(contact)
    }

    private fun ensureTables(profileId: Long) {
        profileDao.ensureTable()
        contactDao.ensureTable()
        chatDao.ensureTable()

        if (profileDao.findByUserId(profileId) == null) {
            profileDao.createDefault(profileId)
        }
    }

    private fun ChatRecord.toChatEntity(contact: ContactRecord): ChatEntity {
        return ChatEntity(
            id = id,
            contactId = contactId,
            title = title,
            isDeleted = isDeleted,
            createdAt = createdAt,
            updatedAt = updatedAt,
            contactEmail = contact.email,
            contactDisplayName = contact.displayName
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
