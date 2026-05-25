package com.example.domain.model

data class SettingsEntity(
    val userId: Long,
    val language: String,
    val hasMailAccessToken: Boolean
)
