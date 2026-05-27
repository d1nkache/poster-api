package com.example.presentation.controller

import com.example.domain.usecase.contact.CreateContactUseCase
import com.example.domain.usecase.contact.DeleteContactUseCase
import com.example.domain.usecase.contact.GetContactUseCase
import com.example.domain.usecase.contact.GetContactsUseCase
import com.example.domain.usecase.contact.GetOrCreateContactChatUseCase
import com.example.domain.usecase.contact.UpdateContactUseCase
import com.example.presentation.mapper.ContactMapper
import com.example.presentation.request.CreateContactRequest
import com.example.presentation.request.UpdateContactRequest
import com.example.presentation.request.toCreateContact
import com.example.presentation.request.toUpdateContact
import com.example.presentation.response.ChatResponse
import com.example.presentation.response.ContactListResponse
import com.example.presentation.response.ContactResponse

class ContactController(
    private val getContactsUseCase: GetContactsUseCase,
    private val getContactUseCase: GetContactUseCase,
    private val createContactUseCase: CreateContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val getOrCreateContactChatUseCase: GetOrCreateContactChatUseCase
) {
    suspend fun getContacts(
        profileId: Long,
        query: String?
    ): ContactListResponse {
        val contacts = getContactsUseCase(profileId, query)

        return ContactMapper.asContactListResponse(contacts)
    }

    suspend fun getContact(
        profileId: Long,
        contactId: Long
    ): ContactResponse? {
        val contact = getContactUseCase(profileId, contactId) ?: return null

        return ContactMapper.asContactResponse(contact)
    }

    suspend fun createContact(
        profileId: Long,
        request: CreateContactRequest
    ): ContactResponse {
        val contact = createContactUseCase(
            profileId,
            request.toCreateContact()
        )

        return ContactMapper.asContactResponse(contact)
    }

    suspend fun updateContact(
        profileId: Long,
        contactId: Long,
        request: UpdateContactRequest
    ): ContactResponse? {
        val contact = updateContactUseCase(
            profileId,
            contactId,
            request.toUpdateContact()
        ) ?: return null

        return ContactMapper.asContactResponse(contact)
    }

    suspend fun deleteContact(
        profileId: Long,
        contactId: Long
    ): Boolean {
        return deleteContactUseCase(profileId, contactId)
    }

    suspend fun getOrCreateChat(
        profileId: Long,
        contactId: Long
    ): ChatResponse? {
        val chat = getOrCreateContactChatUseCase(profileId, contactId) ?: return null

        return ContactMapper.asChatResponse(chat)
    }
}
