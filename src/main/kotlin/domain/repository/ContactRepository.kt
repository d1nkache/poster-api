package com.example.domain.repository

import com.example.domain.model.ContactEntity
import com.example.domain.model.CreateContact
import com.example.domain.model.UpdateContact

interface ContactRepository {
    suspend fun getContacts(
        profileId: Long,
        query: String?
    ): List<ContactEntity>

    suspend fun getContact(
        profileId: Long,
        contactId: Long
    ): ContactEntity?

    suspend fun createContact(
        profileId: Long,
        createContact: CreateContact
    ): ContactEntity

    suspend fun updateContact(
        profileId: Long,
        contactId: Long,
        updateContact: UpdateContact
    ): ContactEntity?

    suspend fun deleteContact(
        profileId: Long,
        contactId: Long
    ): Boolean
}
