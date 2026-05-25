package com.example.presentation.request

import kotlinx.serialization.Serializable

@Serializable
data class SaveMailAccessTokenRequest(
    val token: String
)
