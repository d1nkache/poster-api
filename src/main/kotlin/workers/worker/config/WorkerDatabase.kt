package com.example.workers.worker.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object WorkerDatabase {
    private var dataSource: HikariDataSource? = null

    fun connect(config: DatabaseConfig): Database {
        val hikariDataSource = HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = config.jdbcUrl
                driverClassName = config.driverClassName
                username = config.username
                password = config.password
                maximumPoolSize = config.maximumPoolSize
                minimumIdle = 1
                poolName = "poster-mail-worker-pool"
                validate()
            }
        )
        dataSource = hikariDataSource

        return Database.connect(hikariDataSource)
    }

    fun close() {
        dataSource?.close()
        dataSource = null
    }
}
