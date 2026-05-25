package com.example.presentation.controller

import com.example.domain.model.ToUpload
import com.example.domain.model.UpdateProfile
import com.example.domain.usecase.profile.GetProfileUseCase
import com.example.domain.usecase.profile.UpdateProfileUseCase
import com.example.domain.usecase.profile.UploadProfileAvatarUseCase
import com.example.presentation.mapper.ProfileMapper
import com.example.presentation.request.UpdateProfileRequest
import com.example.presentation.response.ProfileResponse
import com.example.presentation.response.UploadProfileAvatarResponse

class ProfileController(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadProfileAvatarUseCase: UploadProfileAvatarUseCase
) {
    suspend fun getProfile(profileId: Long): ProfileResponse {
        val profile = this.getProfileUseCase(profileId)

        return ProfileMapper.asProfileResponse(profile)
    }

    suspend fun updateProfile(
        profileId: Long,
        request: UpdateProfileRequest
    ): ProfileResponse {
        val profile = this.updateProfileUseCase(
            profileId,
            UpdateProfile(
                name = request.name,
                username = request.username,
                bio = request.bio,
                birthday = request.birthday
            )
        )

        return ProfileMapper.asProfileResponse(profile)
    }

    suspend fun uploadProfileAvatar(
        profileId: Long,
        avatar: ToUpload
    ): UploadProfileAvatarResponse {
        val avatarUrl = this.uploadProfileAvatarUseCase(profileId, avatar)

        return ProfileMapper.asUploadProfileAvatarResponse(avatarUrl)
    }
}
