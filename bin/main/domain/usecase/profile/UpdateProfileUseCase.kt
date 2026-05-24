package com.example.domain.usecase.profile

import com.example.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profileId: Long) = profileRepository.updateProfile(profileId)
}
