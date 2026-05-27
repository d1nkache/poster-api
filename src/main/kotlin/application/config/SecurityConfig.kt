package com.example.application.config

import com.example.domain.service.JwtService
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.installSecurity(jwtService: JwtService = JwtService()) {
    authentication {
        jwt("auth-jwt") {
            realm = JwtService.REALM
            verifier(jwtService.verifier)
            validate { credential ->
                val profileId = credential.payload.subject?.toLongOrNull()
                val type = credential.payload.getClaim("type").asString()

                if (
                    profileId != null &&
                    type == "access" &&
                    credential.payload.audience.contains(JwtService.AUDIENCE)
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
