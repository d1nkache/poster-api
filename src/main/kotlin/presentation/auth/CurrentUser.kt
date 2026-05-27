package com.example.presentation.auth

import com.example.presentation.error.unauthorized
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal

fun ApplicationCall.currentUserId(): Long {
    return principal<JWTPrincipal>()
        ?.payload
        ?.subject
        ?.toLongOrNull()
        ?: unauthorized("UNAUTHORIZED", "Bearer access token is required")
}

fun ApplicationCall.currentProfileId(): Long = currentUserId()
