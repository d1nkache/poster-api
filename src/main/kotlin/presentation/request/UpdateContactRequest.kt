package com.example.presentation.request

import com.example.domain.model.UpdateContact
import kotlinx.serialization.Serializable

@Serializable
data class UpdateContactRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null
)

fun UpdateContactRequest.toUpdateContact(): UpdateContact {
    return UpdateContact(
        displayName = displayName,
        avatarUrl = avatarUrl
    )
}
