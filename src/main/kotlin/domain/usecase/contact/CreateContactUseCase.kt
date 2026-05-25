package com.example.domain.usecase.contact

import com.example.domain.model.CreateContact
import com.example.domain.repository.ContactRepository

class CreateContactUseCase(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        createContact: CreateContact
    ) = contactRepository.createContact(profileId, createContact)
}
