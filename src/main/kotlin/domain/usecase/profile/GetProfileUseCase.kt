package com.example.domain.usecase.profile

import com.example.domain.repository.ProfileRepository

class GetProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profileId: Long) = profileRepository.getProfile(profileId)
}
