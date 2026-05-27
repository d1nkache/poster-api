package com.example.domain.usecase.message

import com.example.domain.model.SendMessage
import com.example.domain.repository.MessageRepository

class SendMessageUseCase(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        chatId: Long,
        sendMessage: SendMessage
    ) = messageRepository.sendMessage(profileId, chatId, sendMessage)
}
