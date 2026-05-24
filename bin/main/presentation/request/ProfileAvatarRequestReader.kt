package com.example.presentation.request

import com.example.domain.model.ProfileAvatarUpload
import com.example.presentation.error.InvalidProfileAvatarException
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

suspend fun ApplicationCall.receiveProfileAvatar(): ProfileAvatarUpload {
    var avatar: ProfileAvatarUpload? = null

    receiveMultipart().forEachPart { part ->
        try {
            if (part is PartData.FileItem && part.name == "avatar") {
                val contentType = part.contentType?.toString()
                    ?: throw InvalidProfileAvatarException(
                        HttpStatusCode.BadRequest,
                        "Avatar content type is required"
                    )

                if (contentType != "image/jpeg") {
                    throw InvalidProfileAvatarException(
                        HttpStatusCode.UnsupportedMediaType,
                        "Avatar must be image/jpeg"
                    )
                }

                avatar = ProfileAvatarUpload(
                    bytes = part.provider().readRemaining().readByteArray(),
                    fileName = sanitizeFileName(part.originalFileName ?: "avatar.jpg"),
                    contentType = contentType
                )
            }
        } finally {
            part.dispose()
        }
    }

    return avatar ?: throw InvalidProfileAvatarException(
        HttpStatusCode.BadRequest,
        "Multipart field avatar is required"
    )
}

private fun sanitizeFileName(fileName: String): String {
    return fileName
        .substringAfterLast('/')
        .substringAfterLast('\\')
        .ifBlank { "avatar.jpg" }
}
