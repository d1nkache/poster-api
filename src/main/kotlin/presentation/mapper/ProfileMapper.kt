package com.example.presentation.mapper

import com.example.domain.model.ProfileEntity
import com.example.presentation.response.ProfileResponse
import com.example.presentation.response.UploadProfileAvatarResponse

object ProfileMapper {
    fun asProfileResponse(profile: ProfileEntity): ProfileResponse {
        return ProfileResponse(
            id = "user-${profile.userId}",
            name = profile.name,
            username = profile.username,
            email = profile.email,
            birthday = profile.birthday,
            bio = profile.bio,
            avatarUrl = profile.avatarUrl,
            isOnline = profile.isOnline
        )
    }

    fun asUploadProfileAvatarResponse(avatarUrl: String): UploadProfileAvatarResponse {
        return UploadProfileAvatarResponse(avatarUrl = avatarUrl)
    }
}
