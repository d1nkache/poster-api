package com.example.domain.repository

import com.example.domain.model.ChatEntity

interface ChatRepository {
    suspend fun getOrCreateChatByContact(
        profileId: Long,
        contactId: Long
    ): ChatEntity?
}
