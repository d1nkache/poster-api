package com.example.domain.usecase.settings

import com.example.domain.model.UpdateSettings
import com.example.domain.repository.SettingsRepository

class UpdateSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        userId: Long,
        updateSettings: UpdateSettings
    ) = settingsRepository.updateSettings(userId, updateSettings)
}
