package com.example.presentation.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateSettingsRequest(
    val language: String? = null
)
