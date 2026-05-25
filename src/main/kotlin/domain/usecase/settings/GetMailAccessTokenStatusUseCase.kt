package com.example.domain.usecase.settings

import com.example.domain.repository.SettingsRepository

class GetMailAccessTokenStatusUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(userId: Long) = settingsRepository.hasMailAccessToken(userId)
}
