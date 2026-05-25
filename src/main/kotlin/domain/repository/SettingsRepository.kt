package com.example.domain.repository

import com.example.domain.model.SettingsEntity
import com.example.domain.model.UpdateSettings

interface SettingsRepository {
    suspend fun getSettings(userId: Long): SettingsEntity

    suspend fun updateSettings(
        userId: Long,
        updateSettings: UpdateSettings
    ): SettingsEntity

    suspend fun saveMailAccessToken(
        userId: Long,
        token: String
    )

    suspend fun deleteMailAccessToken(userId: Long)

    suspend fun hasMailAccessToken(userId: Long): Boolean
}
