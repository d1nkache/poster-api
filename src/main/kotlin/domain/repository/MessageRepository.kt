package com.example.domain.repository

import com.example.domain.model.MessageEntity
import com.example.domain.model.SendMessage

interface MessageRepository {
    suspend fun getChatMessages(
        profileId: Long,
        chatId: Long,
        limit: Int,
        offset: Long
    ): List<MessageEntity>?

    suspend fun sendMessage(
        profileId: Long,
        chatId: Long,
        sendMessage: SendMessage
    ): MessageEntity?

    suspend fun getMessage(
        profileId: Long,
        messageId: Long
    ): MessageEntity?

    suspend fun markMessageAsRead(
        profileId: Long,
        messageId: Long
    ): Boolean

    suspend fun markChatMessagesAsRead(
        profileId: Long,
        chatId: Long
    ): Boolean

    suspend fun deleteMessage(
        profileId: Long,
        messageId: Long
    ): Boolean
}
