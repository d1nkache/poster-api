package com.example.domain.usecase.contact

import com.example.domain.repository.ContactRepository

class GetContactUseCase(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        contactId: Long
    ) = contactRepository.getContact(profileId, contactId)
}
