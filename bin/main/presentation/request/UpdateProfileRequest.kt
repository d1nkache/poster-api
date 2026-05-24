package com.example.presentation.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val birthday: String? = null
)
