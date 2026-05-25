package com.example.domain.usecase.settings

import com.example.domain.repository.SettingsRepository

class SaveMailAccessTokenUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        userId: Long,
        token: String
    ) = settingsRepository.saveMailAccessToken(userId, token)
}
