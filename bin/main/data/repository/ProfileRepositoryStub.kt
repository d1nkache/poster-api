package com.example.data.repository

import com.example.domain.model.ProfileAvatarUpload
import com.example.domain.model.ProfileEntity
import com.example.domain.repository.ProfileRepository

class ProfileRepositoryStub : ProfileRepository {
    override suspend fun getProfile(profileId: Long): ProfileEntity {
        return ProfileEntity()
    }

    override suspend fun updateProfile(profileId: Long): ProfileEntity {
        return ProfileEntity()
    }

    override suspend fun uploadProfileAvatar(
        profileId: Long,
        avatar: ProfileAvatarUpload
    ): String {
        return "/uploads/avatars/$profileId/${avatar.fileName}"
    }
}
