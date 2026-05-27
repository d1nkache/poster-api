package com.example

import com.example.application.config.installDatabase
import com.example.application.config.installHttp
import com.example.application.config.installResources
import com.example.application.config.installRouting
import com.example.application.config.installSecurity
import com.example.application.config.installSerialization
import com.example.application.config.installStatusPages
import com.example.application.config.installWebsockets
import io.ktor.server.application.Application

fun Application.configure() {
    installHttp()
    installSerialization()
    installSecurity()
    installDatabase()
    installStatusPages()
    installWebsockets()
    installResources()
    installRouting()
}
