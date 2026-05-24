package com.example.presentation.auth

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header

fun ApplicationCall.currentProfileId(): Long {
    return request.header("X-Profile-Id")?.toLongOrNull() ?: 1L
}
