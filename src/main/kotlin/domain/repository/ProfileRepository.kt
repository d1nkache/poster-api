package com.example.domain.repository

import com.example.domain.model.ProfileEntity
import com.example.domain.model.ToUpload
import com.example.domain.model.UpdateProfile

interface ProfileRepository {
    suspend fun getProfile(profileId: Long): ProfileEntity

    suspend fun updateProfile(
        profileId: Long,
        updateProfile: UpdateProfile
    ): ProfileEntity

    suspend fun uploadProfileAvatar(
        profileId: Long,
        avatar: ToUpload
    ): String
}
