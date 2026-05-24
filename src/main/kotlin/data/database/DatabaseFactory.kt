package com.example.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    private var dataSource: HikariDataSource? = null

    fun init(config: ApplicationConfig): Database {
        val hikariDataSource = createDataSource(config)
        dataSource = hikariDataSource

        return Database.connect(hikariDataSource)
    }

    fun close() {
        dataSource?.close()
        dataSource = null
    }

    private fun createDataSource(config: ApplicationConfig): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.property("database.jdbcUrl").getString()
            driverClassName = config.property("database.driverClassName").getString()
            username = config.property("database.username").getString()
            password = config.property("database.password").getString()
            maximumPoolSize = config.propertyOrNull("database.maximumPoolSize")
                ?.getString()
                ?.toInt()
                ?: 10
            minimumIdle = config.propertyOrNull("database.minimumIdle")
                ?.getString()
                ?.toInt()
                ?: 2
            poolName = "posterDb-pool"
            validate()
        }

        return HikariDataSource(hikariConfig)
    }
}
