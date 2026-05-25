package com.example.presentation.response

import kotlinx.serialization.Serializable

@Serializable
data class ContactListResponse(
    val items: List<ContactResponse>
)
