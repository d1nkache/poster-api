package com.example.presentation.auth

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal

fun ApplicationCall.currentProfileId(): Long {
    return requireNotNull(principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()) {
        "Missing authenticated profile"
    }
}
