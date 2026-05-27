package com.example.presentation.error

import io.ktor.http.HttpStatusCode

class InvalidProfileAvatarException(
    statusCode: HttpStatusCode,
    code: String,
    override val message: String
) : ApiException(statusCode, code, message)
