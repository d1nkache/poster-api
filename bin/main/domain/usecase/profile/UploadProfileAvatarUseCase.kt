package com.example.domain.usecase.profile

import com.example.domain.model.ProfileAvatarUpload
import com.example.domain.repository.ProfileRepository

class UploadProfileAvatarUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        avatar: ProfileAvatarUpload
    ) = profileRepository.uploadProfileAvatar(profileId, avatar)
}
