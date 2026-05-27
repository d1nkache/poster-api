package com.example.data.dao

import com.example.data.database.table.ProfilesTable
import com.example.data.database.table.SettingsTable
import com.example.domain.model.UpdateSettings
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class SettingsDao {
    fun ensureTable() {
        SchemaUtils.createMissingTablesAndColumns(SettingsTable)
    }

    fun findByUserId(userId: Long): SettingsRecord? {
        return SettingsTable
            .selectAll()
            .where { SettingsTable.userId eq userId }
            .singleOrNull()
            ?.toSettingsRecord()
    }

    fun createDefault(userId: Long): SettingsRecord {
        SettingsTable.insert { statement ->
            statement[SettingsTable.userId] = userId
            statement[language] = DEFAULT_LANGUAGE
            statement[mailAccessToken] = null
        }

        return requireNotNull(findByUserId(userId))
    }

    fun update(userId: Long, updateSettings: UpdateSettings): SettingsRecord {
        SettingsTable.update({ SettingsTable.userId eq userId }) { statement ->
            updateSettings.language?.let { statement[language] = it }
        }

        return requireNotNull(findByUserId(userId))
    }

    fun saveMailAccessToken(userId: Long, token: String) {
        SettingsTable.update({ SettingsTable.userId eq userId }) { statement ->
            statement[mailAccessToken] = token
        }
        ProfilesTable.update({ ProfilesTable.userId eq userId }) { statement ->
            statement[mailSyncEnabled] = true
        }
    }

    fun deleteMailAccessToken(userId: Long) {
        SettingsTable.update({ SettingsTable.userId eq userId }) { statement ->
            statement[mailAccessToken] = null
        }
        ProfilesTable.update({ ProfilesTable.userId eq userId }) { statement ->
            statement[mailSyncEnabled] = false
        }
    }

    private fun ResultRow.toSettingsRecord(): SettingsRecord {
        return SettingsRecord(
            userId = this[SettingsTable.userId],
            language = this[SettingsTable.language],
            mailAccessToken = this[SettingsTable.mailAccessToken]
        )
    }

    private companion object {
        const val DEFAULT_LANGUAGE = "en"
    }
}

data class SettingsRecord(
    val userId: Long,
    val language: String,
    val mailAccessToken: String?
)
