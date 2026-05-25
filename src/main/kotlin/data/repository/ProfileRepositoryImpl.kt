package com.example.data.repository

import com.example.data.dao.ProfileDao
import com.example.data.dao.ProfileRecord
import com.example.domain.model.ProfileEntity
import com.example.domain.model.ToUpload
import com.example.domain.model.UpdateProfile
import com.example.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path


class ProfileRepositoryImpl(
    private val profileDao: ProfileDao,
    private val mediaRoot: Path = Path.of("media")
) : ProfileRepository {
    override suspend fun getProfile(profileId: Long): ProfileEntity = dbQuery {
        profileDao.ensureTable()
        val profile = profileDao.findByUserId(profileId) ?: profileDao.createDefault(profileId)

        profile.toProfileEntity()
    }

    override suspend fun updateProfile(
        profileId: Long,
        updateProfile: UpdateProfile
    ): ProfileEntity = dbQuery {
        profileDao.ensureTable()
        if (profileDao.findByUserId(profileId) == null) {
            profileDao.createDefault(profileId)
        }

        profileDao.update(profileId, updateProfile).toProfileEntity()
    }

    override suspend fun uploadProfileAvatar(
        profileId: Long,
        avatar: ToUpload
    ): String = withContext(Dispatchers.IO) {
        val avatarPath = avatarPath(profileId)
        Files.createDirectories(avatarPath.parent)
        Files.write(avatarPath, avatar.bytes)

        avatarUrl(profileId)
    }

    private fun ProfileRecord.toProfileEntity(): ProfileEntity {
        return ProfileEntity(
            userId = userId,
            name = name,
            username = username,
            email = email,
            birthday = birthday,
            bio = bio,
            avatarUrl = avatarUrl(userId),
            isOnline = isOnline
        )
    }

    private fun avatarPath(profileId: Long): Path {
        return mediaRoot.resolve("avatars").resolve(avatarFileName(profileId))
    }

    private fun avatarUrl(profileId: Long): String {
        return "/media/avatars/${avatarFileName(profileId)}"
    }

    private fun avatarFileName(profileId: Long): String {
        return "profile-$profileId-avatar.png"
    }

    private suspend fun <T> dbQuery(toBlock: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction {
                toBlock()
            }
        }
    }
}
