package com.example.domain.usecase.profile

import com.example.domain.model.UpdateProfile
import com.example.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        updateProfile: UpdateProfile
    ) = profileRepository.updateProfile(profileId, updateProfile)
}
