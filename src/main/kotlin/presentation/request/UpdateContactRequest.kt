package com.example.presentation.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateContactRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null
)
