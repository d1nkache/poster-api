package com.example.domain.usecase.contact

import com.example.domain.model.UpdateContact
import com.example.domain.repository.ContactRepository

class UpdateContactUseCase(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        contactId: Long,
        updateContact: UpdateContact
    ) = contactRepository.updateContact(profileId, contactId, updateContact)
}
