package com.example.domain.usecase.profile

import com.example.domain.model.ToUpload
import com.example.domain.repository.ProfileRepository

class UploadProfileAvatarUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        profileId: Long,
        avatar: ToUpload
    ) = profileRepository.uploadProfileAvatar(profileId, avatar)
}
