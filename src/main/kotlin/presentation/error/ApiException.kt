package com.example.presentation.error

import io.ktor.http.HttpStatusCode

open class ApiException(
    val statusCode: HttpStatusCode,
    val code: String,
    override val message: String
) : RuntimeException(message)

fun badRequest(code: String, message: String): Nothing {
    throw ApiException(HttpStatusCode.BadRequest, code, message)
}

fun conflict(code: String, message: String): Nothing {
    throw ApiException(HttpStatusCode.Conflict, code, message)
}

fun notFound(code: String, message: String): Nothing {
    throw ApiException(HttpStatusCode.NotFound, code, message)
}

fun unauthorized(code: String, message: String): Nothing {
    throw ApiException(HttpStatusCode.Unauthorized, code, message)
}

fun unsupportedMediaType(code: String, message: String): Nothing {
    throw ApiException(HttpStatusCode.UnsupportedMediaType, code, message)
}
