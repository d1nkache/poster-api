package com.example.domain.usecase.contact

import com.example.domain.repository.ContactRepository

class GetContactsUseCase(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        query: String?
    ) = contactRepository.getContacts(profileId, query)
}
