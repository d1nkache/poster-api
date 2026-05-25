package com.example.domain.usecase.settings

import com.example.domain.repository.SettingsRepository

class GetSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(userId: Long) = settingsRepository.getSettings(userId)
}
