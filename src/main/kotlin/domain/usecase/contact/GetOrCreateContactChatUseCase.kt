package com.example.domain.usecase.contact

import com.example.domain.repository.ChatRepository

class GetOrCreateContactChatUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        contactId: Long
    ) = chatRepository.getOrCreateChatByContact(profileId, contactId)
}
