package com.example.data.dao

import com.example.data.database.table.ProfilesTable
import com.example.domain.model.MailSyncStatus
import com.example.domain.model.RegisterProfile
import com.example.domain.model.UpdateProfile
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ProfileDao {
    fun ensureTable() {
        SchemaUtils.createMissingTablesAndColumns(ProfilesTable)
    }

    fun findByUserId(userId: Long): ProfileRecord? {
        return ProfilesTable
            .selectAll()
            .where { ProfilesTable.userId eq userId }
            .singleOrNull()
            ?.toProfileRecord()
    }

    fun findByEmail(email: String): ProfileRecord? {
        return ProfilesTable
            .selectAll()
            .where { ProfilesTable.email eq email.lowercase() }
            .singleOrNull()
            ?.toProfileRecord()
    }

    fun findMailSyncEnabled(): List<ProfileRecord> {
        return ProfilesTable
            .selectAll()
            .where { ProfilesTable.mailSyncEnabled eq true }
            .map { it.toProfileRecord() }
    }

    fun findAll(): List<ProfileRecord> {
        return ProfilesTable
            .selectAll()
            .map { it.toProfileRecord() }
    }

    fun createDefault(userId: Long): ProfileRecord {
        val now = Instant.now().toString()
        ProfilesTable.insert { statement ->
            statement[ProfilesTable.userId] = userId
            statement[name] = "User $userId"
            statement[username] = "@user$userId"
            statement[email] = "user$userId@mail.com"
            statement[passwordHash] = null
            statement[birthday] = null
            statement[bio] = null
            statement[isOnline] = true
            statement[isVerified] = true
            statement[otpCodeHash] = null
            statement[otpExpiresAt] = null
            statement[refreshTokenHash] = null
            statement[createdAt] = now
            statement[updatedAt] = now
            statement[mailSyncEnabled] = false
            statement[lastUid] = null
            statement[lastSyncAt] = null
            statement[syncStatus] = MailSyncStatus.IDLE.name
        }

        return requireNotNull(findByUserId(userId))
    }

    fun createRegistered(
        registerProfile: RegisterProfile,
        passwordHash: String,
        otpCodeHash: String,
        otpExpiresAt: String
    ): ProfileRecord {
        val userId = nextUserId()
        val now = Instant.now().toString()
        val normalizedEmail = registerProfile.email.lowercase()

        ProfilesTable.insert { statement ->
            statement[ProfilesTable.userId] = userId
            statement[name] = registerProfile.displayName ?: normalizedEmail.substringBefore("@")
            statement[username] = "@user$userId"
            statement[email] = normalizedEmail
            statement[ProfilesTable.passwordHash] = passwordHash
            statement[birthday] = null
            statement[bio] = null
            statement[isOnline] = false
            statement[isVerified] = false
            statement[ProfilesTable.otpCodeHash] = otpCodeHash
            statement[ProfilesTable.otpExpiresAt] = otpExpiresAt
            statement[refreshTokenHash] = null
            statement[createdAt] = now
            statement[updatedAt] = now
            statement[mailSyncEnabled] = false
            statement[lastUid] = null
            statement[lastSyncAt] = null
            statement[syncStatus] = MailSyncStatus.IDLE.name
        }

        return requireNotNull(findByUserId(userId))
    }

    fun saveOtp(
        userId: Long,
        otpCodeHash: String,
        otpExpiresAt: String
    ) {
        ProfilesTable.update({ ProfilesTable.userId eq userId }) { statement ->
            statement[ProfilesTable.otpCodeHash] = otpCodeHash
            statement[ProfilesTable.otpExpiresAt] = otpExpiresAt
            statement[updatedAt] = Instant.now().toString()
        }
    }

    fun verifyEmail(userId: Long) {
        ProfilesTable.update({ ProfilesTable.userId eq userId }) { statement ->
            statement[isVerified] = true
            statement[otpCodeHash] = null
            statement[otpExpiresAt] = null
            statement[updatedAt] = Instant.now().toString()
        }
    }

    fun saveRefreshTokenHash(userId: Long, refreshTokenHash: String?) {
        ProfilesTable.update({ ProfilesTable.userId eq userId }) { statement ->
            statement[ProfilesTable.refreshTokenHash] = refreshTokenHash
            statement[updatedAt] = Instant.now().toString()
        }
    }

    fun updateMailSyncState(
        userId: Long,
        lastUid: Long?,
        syncStatus: MailSyncStatus
    ) {
        ProfilesTable.update({ ProfilesTable.userId eq userId }) { statement ->
            lastUid?.let { statement[ProfilesTable.lastUid] = it }
            statement[lastSyncAt] = Instant.now().toString()
            statement[ProfilesTable.syncStatus] = syncStatus.name
        }
    }

    fun updateMailSyncStatus(userId: Long, syncStatus: MailSyncStatus) {
        ProfilesTable.update({ ProfilesTable.userId eq userId }) { statement ->
            statement[ProfilesTable.syncStatus] = syncStatus.name
            statement[lastSyncAt] = Instant.now().toString()
        }
    }

    fun update(userId: Long, updateProfile: UpdateProfile): ProfileRecord {
        ProfilesTable.update({ ProfilesTable.userId eq userId }) { statement ->
            updateProfile.name?.let { statement[name] = it }
            updateProfile.username?.let { statement[username] = it }
            updateProfile.birthday?.let { statement[birthday] = it }
            updateProfile.bio?.let { statement[bio] = it }
            statement[updatedAt] = Instant.now().toString()
        }

        return requireNotNull(findByUserId(userId))
    }

    private fun ResultRow.toProfileRecord(): ProfileRecord {
        return ProfileRecord(
            userId = this[ProfilesTable.userId],
            name = this[ProfilesTable.name],
            username = this[ProfilesTable.username],
            email = this[ProfilesTable.email],
            passwordHash = this[ProfilesTable.passwordHash],
            birthday = this[ProfilesTable.birthday],
            bio = this[ProfilesTable.bio],
            isOnline = this[ProfilesTable.isOnline],
            isVerified = this[ProfilesTable.isVerified],
            otpCodeHash = this[ProfilesTable.otpCodeHash],
            otpExpiresAt = this[ProfilesTable.otpExpiresAt],
            refreshTokenHash = this[ProfilesTable.refreshTokenHash],
            createdAt = this[ProfilesTable.createdAt],
            updatedAt = this[ProfilesTable.updatedAt],
            mailSyncEnabled = this[ProfilesTable.mailSyncEnabled],
            lastUid = this[ProfilesTable.lastUid],
            lastSyncAt = this[ProfilesTable.lastSyncAt],
            syncStatus = MailSyncStatus.valueOf(this[ProfilesTable.syncStatus])
        )
    }

    private fun nextUserId(): Long {
        return (ProfilesTable.selectAll().map { it[ProfilesTable.userId] }.maxOrNull() ?: 0L) + 1L
    }
}

data class ProfileRecord(
    val userId: Long,
    val name: String,
    val username: String,
    val email: String,
    val passwordHash: String?,
    val birthday: String?,
    val bio: String?,
    val isOnline: Boolean,
    val isVerified: Boolean,
    val otpCodeHash: String?,
    val otpExpiresAt: String?,
    val refreshTokenHash: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val mailSyncEnabled: Boolean,
    val lastUid: Long?,
    val lastSyncAt: String?,
    val syncStatus: MailSyncStatus
)
