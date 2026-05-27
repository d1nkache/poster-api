package com.example.presentation.request

import com.example.domain.model.ToUpload
import com.example.presentation.error.InvalidProfileAvatarException
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray



/*
ПРИМЕР заголовков multipart запроса:

POST /profile/avatar HTTP/1.1
Host: localhost:8080
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Length: 45230
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="avatar"; filename="my_photo.png"
Content-Type: image/png

PNG
IHDR        IDATx^ݵ	%G `I `I `I `I `I `I `
*/

suspend fun ApplicationCall.receiveProfileAvatar(): ToUpload {
    var avatar: ToUpload? = null

    receiveMultipart().forEachPart { part ->
        try {
            if (part is PartData.FileItem && part.name == "avatar") {
                val contentType = part.contentType?.toString()
                    ?: throw InvalidProfileAvatarException(
                        HttpStatusCode.BadRequest,
                        "AVATAR_CONTENT_TYPE_REQUIRED",
                        "Avatar content type is required"
                    )

                if (contentType != "image/png") {
                    throw InvalidProfileAvatarException(
                        HttpStatusCode.UnsupportedMediaType,
                        "UNSUPPORTED_AVATAR_TYPE",
                        "Avatar must be image/png"
                    )
                }

                avatar = ToUpload(
                    bytes = part.provider().readRemaining().readByteArray(),
                    contentType = contentType
                )
            }
        } finally {
            part.dispose()
        }
    }

    return avatar ?: throw InvalidProfileAvatarException(
        HttpStatusCode.BadRequest,
        "AVATAR_FIELD_REQUIRED",
        "Multipart field avatar is required"
    )
}
