package com.example.domain.repository

import com.example.domain.model.ProfileAvatarUpload
import com.example.domain.model.ProfileEntity

interface ProfileRepository {
    suspend fun getProfile(profileId: Long): ProfileEntity

    suspend fun updateProfile(profileId: Long): ProfileEntity

    suspend fun uploadProfileAvatar(
        profileId: Long,
        avatar: ProfileAvatarUpload
    ): String
}
