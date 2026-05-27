package com.example.domain.usecase.message

import com.example.domain.repository.MessageRepository

class DeleteMessageUseCase(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        messageId: Long
    ) = messageRepository.deleteMessage(profileId, messageId)
}
