package com.example.domain.usecase.message

import com.example.domain.repository.MessageRepository

class MarkChatMessagesAsReadUseCase(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        chatId: Long
    ) = messageRepository.markChatMessagesAsRead(profileId, chatId)
}
