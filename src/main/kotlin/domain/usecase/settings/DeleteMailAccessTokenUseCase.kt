package com.example.domain.usecase.settings

import com.example.domain.repository.SettingsRepository

class DeleteMailAccessTokenUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(userId: Long) = settingsRepository.deleteMailAccessToken(userId)
}
