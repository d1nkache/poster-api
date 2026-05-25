package com.example.domain.usecase.contact

import com.example.domain.repository.ContactRepository

class DeleteContactUseCase(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        contactId: Long
    ) = contactRepository.deleteContact(profileId, contactId)
}
