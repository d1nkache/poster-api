package com.example.data.repository

import com.example.data.dao.ProfileDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.SettingsRecord
import com.example.domain.model.SettingsEntity
import com.example.domain.model.UpdateSettings
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao,
    private val profileDao: ProfileDao
) : SettingsRepository {
    override suspend fun getSettings(userId: Long): SettingsEntity = dbQuery {
        getOrCreateSettings(userId).toSettingsEntity()
    }

    override suspend fun updateSettings(
        userId: Long,
        updateSettings: UpdateSettings
    ): SettingsEntity = dbQuery {
        getOrCreateSettings(userId)

        settingsDao.update(userId, updateSettings).toSettingsEntity()
    }

    override suspend fun saveMailAccessToken(
        userId: Long,
        token: String
    ): Unit = dbQuery {
        getOrCreateSettings(userId)
        settingsDao.saveMailAccessToken(userId, token)
    }

    override suspend fun deleteMailAccessToken(userId: Long): Unit = dbQuery {
        getOrCreateSettings(userId)
        settingsDao.deleteMailAccessToken(userId)
    }

    override suspend fun hasMailAccessToken(userId: Long): Boolean = dbQuery {
        getOrCreateSettings(userId).mailAccessToken != null
    }

    private fun getOrCreateSettings(userId: Long): SettingsRecord {
        profileDao.ensureTable()
        settingsDao.ensureTable()

        if (profileDao.findByUserId(userId) == null) {
            profileDao.createDefault(userId)
        }

        return settingsDao.findByUserId(userId) ?: settingsDao.createDefault(userId)
    }

    private fun SettingsRecord.toSettingsEntity(): SettingsEntity {
        return SettingsEntity(
            userId = userId,
            language = language,
            hasMailAccessToken = mailAccessToken != null
        )
    }

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction {
                block()
            }
        }
    }
}
