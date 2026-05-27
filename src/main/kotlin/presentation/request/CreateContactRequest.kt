package com.example.presentation.request

import com.example.domain.model.CreateContact
import kotlinx.serialization.Serializable

@Serializable
data class CreateContactRequest(
    val email: String,
    val displayName: String? = null
)

fun CreateContactRequest.toCreateContact(): CreateContact {
    return CreateContact(
        email = email,
        displayName = displayName
    )
}
