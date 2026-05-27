package com.example.presentation.mapper

import com.example.domain.model.MessageEntity
import com.example.presentation.response.MessageListResponse
import com.example.presentation.response.MessageResponse
import com.example.presentation.response.SendMessageResponse

object MessageMapper {
    fun asMessageResponse(message: MessageEntity): MessageResponse {
        return MessageResponse(
            id = message.id,
            chatId = message.chatId,
            bodyText = message.bodyText,
            direction = message.direction,
            status = message.status,
            isRead = message.isRead,
            createdAt = message.createdAt,
            sentAt = message.sentAt,
            receivedAt = message.receivedAt
        )
    }

    fun asMessageListResponse(messages: List<MessageEntity>): MessageListResponse {
        return MessageListResponse(items = messages.map { asMessageResponse(it) })
    }

    fun asSendMessageResponse(message: MessageEntity): SendMessageResponse {
        return SendMessageResponse(message = asMessageResponse(message))
    }
}
