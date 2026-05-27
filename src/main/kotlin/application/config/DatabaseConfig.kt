package com.example.application.config

import com.example.data.database.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped

fun Application.installDatabase() {
    DatabaseFactory.init(environment.config)

    monitor.subscribe(ApplicationStopped) {
        DatabaseFactory.close()
    }
}
