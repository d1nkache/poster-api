package com.example.application.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.websocket.WebSockets

fun Application.installWebsockets() {
    install(WebSockets) {
        pingPeriodMillis = 15_000L
        timeoutMillis = 15_000L
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}
