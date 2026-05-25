package com.example.presentation.mapper

import com.example.domain.model.ChatEntity
import com.example.domain.model.ContactEntity
import com.example.presentation.response.ChatResponse
import com.example.presentation.response.ContactListResponse
import com.example.presentation.response.ContactResponse

object ContactMapper {
    fun asContactResponse(contact: ContactEntity): ContactResponse {
        return ContactResponse(
            id = contact.id,
            email = contact.email,
            displayName = contact.displayName,
            avatarUrl = contact.avatarUrl,
            createdAt = contact.createdAt,
            updatedAt = contact.updatedAt
        )
    }

    fun asContactListResponse(contacts: List<ContactEntity>): ContactListResponse {
        return ContactListResponse(items = contacts.map { asContactResponse(it) })
    }

    fun asChatResponse(chat: ChatEntity): ChatResponse {
        return ChatResponse(
            id = "chat-${chat.id}",
            title = chat.title,
            initials = initials(chat.contactDisplayName ?: chat.contactEmail),
            lastMessage = null,
            lastMessageTime = null,
            unreadCount = 0,
            isOnline = false
        )
    }

    private fun initials(value: String): String {
        return value
            .split(" ", "@", ".", "-", "_")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "?" }
    }
}
