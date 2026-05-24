package com.example.presentation.mapper

import com.example.domain.model.ProfileEntity
import com.example.presentation.response.ProfileResponse
import com.example.presentation.response.UpdateProfileResponse
import com.example.presentation.response.UploadProfileAvatarResponse

object ProfileMapper {
    fun asProfileResponse(profile: ProfileEntity): ProfileResponse {
        return ProfileResponse(
            userId = profile.userId,
            username = profile.username,
            bio = profile.bio,
            avatarUrl = profile.avatarUrl
        )
    }

    fun asUpdateProfileResponse(profile: ProfileEntity): UpdateProfileResponse {
        return UpdateProfileResponse(
            userId = profile.userId,
            username = profile.username,
            bio = profile.bio,
            avatarUrl = profile.avatarUrl
        )
    }

    fun asUploadProfileAvatarResponse(avatarUrl: String): UploadProfileAvatarResponse {
        return UploadProfileAvatarResponse(avatarUrl = avatarUrl)
    }
}
