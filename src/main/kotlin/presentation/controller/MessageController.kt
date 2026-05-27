package com.example.presentation.controller

import com.example.domain.usecase.message.DeleteMessageUseCase
import com.example.domain.usecase.message.GetChatMessagesUseCase
import com.example.domain.usecase.message.GetMessageUseCase
import com.example.domain.usecase.message.MarkChatMessagesAsReadUseCase
import com.example.domain.usecase.message.MarkMessageAsReadUseCase
import com.example.domain.usecase.message.SendMessageUseCase
import com.example.presentation.mapper.MessageMapper
import com.example.presentation.request.SendMessageRequest
import com.example.presentation.request.toSendMessage
import com.example.presentation.response.MessageListResponse
import com.example.presentation.response.MessageResponse
import com.example.presentation.response.SendMessageResponse

class MessageController(
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessageUseCase: GetMessageUseCase,
    private val markMessageAsReadUseCase: MarkMessageAsReadUseCase,
    private val markChatMessagesAsReadUseCase: MarkChatMessagesAsReadUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase
) {
    suspend fun getChatMessages(
        profileId: Long,
        chatId: Long,
        limit: Int,
        offset: Long
    ): MessageListResponse? {
        val messages = getChatMessagesUseCase(profileId, chatId, limit, offset) ?: return null

        return MessageMapper.asMessageListResponse(messages)
    }

    suspend fun sendMessage(
        profileId: Long,
        chatId: Long,
        request: SendMessageRequest
    ): SendMessageResponse? {
        val message = sendMessageUseCase(profileId, chatId, request.toSendMessage()) ?: return null

        return MessageMapper.asSendMessageResponse(message)
    }

    suspend fun getMessage(
        profileId: Long,
        messageId: Long
    ): MessageResponse? {
        val message = getMessageUseCase(profileId, messageId) ?: return null

        return MessageMapper.asMessageResponse(message)
    }

    suspend fun markMessageAsRead(
        profileId: Long,
        messageId: Long
    ): Boolean {
        return markMessageAsReadUseCase(profileId, messageId)
    }

    suspend fun markChatMessagesAsRead(
        profileId: Long,
        chatId: Long
    ): Boolean {
        return markChatMessagesAsReadUseCase(profileId, chatId)
    }

    suspend fun deleteMessage(
        profileId: Long,
        messageId: Long
    ): Boolean {
        return deleteMessageUseCase(profileId, messageId)
    }
}
