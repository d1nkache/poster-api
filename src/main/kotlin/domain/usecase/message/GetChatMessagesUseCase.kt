package com.example.domain.usecase.message

import com.example.domain.repository.MessageRepository

class GetChatMessagesUseCase(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        chatId: Long,
        limit: Int,
        offset: Long
    ) = messageRepository.getChatMessages(profileId, chatId, limit, offset)
}
