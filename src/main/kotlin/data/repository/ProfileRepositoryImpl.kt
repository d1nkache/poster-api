package com.example.data.repository

import com.example.data.database.table.ProfilesTable
import com.example.domain.model.ProfileEntity
import com.example.domain.model.ToUpload
import com.example.domain.model.UpdateProfile
import com.example.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.nio.file.Files
import java.nio.file.Path

class ProfileRepositoryImpl(
    private val mediaRoot: Path = Path.of("media")
) : ProfileRepository {
    override suspend fun getProfile(profileId: Long): ProfileEntity = dbQuery {
        ensureProfilesTable()
        findProfile(profileId) ?: createProfile(profileId)
    }

    override suspend fun updateProfile(
        profileId: Long,
        updateProfile: UpdateProfile
    ): ProfileEntity = dbQuery {
        ensureProfilesTable()
        if (findProfile(profileId) == null) {
            createProfile(profileId)
        }

        ProfilesTable.update({ ProfilesTable.userId eq profileId }) { statement ->
            updateProfile.username?.let { statement[username] = it }
            updateProfile.bio?.let { statement[bio] = it }
        }

        requireNotNull(findProfile(profileId))
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

    private fun ensureProfilesTable() {
        SchemaUtils.createMissingTablesAndColumns(ProfilesTable)
    }

    private fun findProfile(profileId: Long): ProfileEntity? {
        return ProfilesTable
            .selectAll()
            .where { ProfilesTable.userId eq profileId }
            .singleOrNull()
            ?.toProfileEntity()
    }

    private fun createProfile(profileId: Long): ProfileEntity {
        ProfilesTable.insert { statement ->
            statement[userId] = profileId
            statement[username] = "user_$profileId"
            statement[bio] = null
        }

        return requireNotNull(findProfile(profileId))
    }

    private fun ResultRow.toProfileEntity(): ProfileEntity {
        val profileId = this[ProfilesTable.userId]

        return ProfileEntity(
            userId = profileId,
            username = this[ProfilesTable.username],
            bio = this[ProfilesTable.bio],
            avatarUrl = avatarUrl(profileId)
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

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction {
                block()
            }
        }
    }
}
