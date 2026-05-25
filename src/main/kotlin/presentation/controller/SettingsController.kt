package com.example.presentation.controller

import com.example.domain.model.UpdateSettings
import com.example.domain.usecase.settings.DeleteMailAccessTokenUseCase
import com.example.domain.usecase.settings.GetMailAccessTokenStatusUseCase
import com.example.domain.usecase.settings.GetSettingsUseCase
import com.example.domain.usecase.settings.SaveMailAccessTokenUseCase
import com.example.domain.usecase.settings.UpdateSettingsUseCase
import com.example.presentation.mapper.SettingsMapper
import com.example.presentation.request.SaveMailAccessTokenRequest
import com.example.presentation.request.UpdateSettingsRequest
import com.example.presentation.response.MailAccessTokenStatusResponse
import com.example.presentation.response.SettingsResponse

class SettingsController(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val saveMailAccessTokenUseCase: SaveMailAccessTokenUseCase,
    private val deleteMailAccessTokenUseCase: DeleteMailAccessTokenUseCase,
    private val getMailAccessTokenStatusUseCase: GetMailAccessTokenStatusUseCase
) {
    suspend fun getSettings(userId: Long): SettingsResponse {
        val settings = getSettingsUseCase(userId)

        return SettingsMapper.asSettingsResponse(settings)
    }

    suspend fun updateSettings(
        userId: Long,
        request: UpdateSettingsRequest
    ): SettingsResponse {
        val settings = updateSettingsUseCase(
            userId,
            UpdateSettings(language = request.language)
        )

        return SettingsMapper.asSettingsResponse(settings)
    }

    suspend fun saveMailAccessToken(
        userId: Long,
        request: SaveMailAccessTokenRequest
    ) {
        saveMailAccessTokenUseCase(userId, request.token)
    }

    suspend fun deleteMailAccessToken(userId: Long) {
        deleteMailAccessTokenUseCase(userId)
    }

    suspend fun getMailAccessTokenStatus(userId: Long): MailAccessTokenStatusResponse {
        val configured = getMailAccessTokenStatusUseCase(userId)

        return SettingsMapper.asMailAccessTokenStatusResponse(configured)
    }
}
