package com.example.data.dao

import com.example.data.database.table.ProfilesTable
import com.example.domain.model.MailSyncStatus
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

    fun findMailSyncEnabled(): List<ProfileRecord> {
        return ProfilesTable
            .selectAll()
            .where { ProfilesTable.mailSyncEnabled eq true }
            .map { it.toProfileRecord() }
    }

    fun createDefault(userId: Long): ProfileRecord {
        ProfilesTable.insert { statement ->
            statement[ProfilesTable.userId] = userId
            statement[name] = "User $userId"
            statement[username] = "@user$userId"
            statement[email] = "user$userId@mail.com"
            statement[birthday] = null
            statement[bio] = null
            statement[isOnline] = true
            statement[mailSyncEnabled] = false
            statement[lastUid] = null
            statement[lastSyncAt] = null
            statement[syncStatus] = MailSyncStatus.IDLE.name
        }

        return requireNotNull(findByUserId(userId))
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
        }

        return requireNotNull(findByUserId(userId))
    }

    private fun ResultRow.toProfileRecord(): ProfileRecord {
        return ProfileRecord(
            userId = this[ProfilesTable.userId],
            name = this[ProfilesTable.name],
            username = this[ProfilesTable.username],
            email = this[ProfilesTable.email],
            birthday = this[ProfilesTable.birthday],
            bio = this[ProfilesTable.bio],
            isOnline = this[ProfilesTable.isOnline],
            mailSyncEnabled = this[ProfilesTable.mailSyncEnabled],
            lastUid = this[ProfilesTable.lastUid],
            lastSyncAt = this[ProfilesTable.lastSyncAt],
            syncStatus = MailSyncStatus.valueOf(this[ProfilesTable.syncStatus])
        )
    }
}

data class ProfileRecord(
    val userId: Long,
    val name: String,
    val username: String,
    val email: String,
    val birthday: String?,
    val bio: String?,
    val isOnline: Boolean,
    val mailSyncEnabled: Boolean,
    val lastUid: Long?,
    val lastSyncAt: String?,
    val syncStatus: MailSyncStatus
)
