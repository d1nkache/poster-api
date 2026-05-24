package com.example

import com.example.data.database.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped

fun Application.configureDatabase() {
    DatabaseFactory.init(environment.config)

    monitor.subscribe(ApplicationStopped) {
        DatabaseFactory.close()
    }
}
