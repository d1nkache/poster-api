package com.example.presentation.mapper

import com.example.domain.model.ProfileEntity
import com.example.presentation.response.ProfileResponse
import com.example.presentation.response.UpdateProfileResponse
import com.example.presentation.response.UploadProfileAvatarResponse

object ProfileMapper {
    fun asProfileResponse(profile: ProfileEntity): ProfileResponse {
        return ProfileResponse()
    }

    fun asUpdateProfileResponse(profile: ProfileEntity): UpdateProfileResponse {
        return UpdateProfileResponse()
    }

    fun asUploadProfileAvatarResponse(avatarUrl: String): UploadProfileAvatarResponse {
        return UploadProfileAvatarResponse(avatarUrl = avatarUrl)
    }
}
