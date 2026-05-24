package com.example.presentation.error

import io.ktor.http.HttpStatusCode

class InvalidProfileAvatarException(
    val statusCode: HttpStatusCode,
    override val message: String
) : RuntimeException(message)
