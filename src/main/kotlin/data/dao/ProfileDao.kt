package com.example.data.dao

import com.example.data.database.table.ProfilesTable
import com.example.domain.model.UpdateProfile
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

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

    fun createDefault(userId: Long): ProfileRecord {
        ProfilesTable.insert { statement ->
            statement[ProfilesTable.userId] = userId
            statement[name] = "User $userId"
            statement[username] = "@user$userId"
            statement[email] = "user$userId@mail.com"
            statement[birthday] = null
            statement[bio] = null
            statement[isOnline] = true
        }

        return requireNotNull(findByUserId(userId))
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
            isOnline = this[ProfilesTable.isOnline]
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
    val isOnline: Boolean
)
