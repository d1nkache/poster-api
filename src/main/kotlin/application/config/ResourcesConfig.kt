package com.example.application.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.resources.Resources

fun Application.installResources() {
    install(Resources)
}
