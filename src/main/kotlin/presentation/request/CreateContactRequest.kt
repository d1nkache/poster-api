package com.example.presentation.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateContactRequest(
    val email: String,
    val displayName: String? = null
)
