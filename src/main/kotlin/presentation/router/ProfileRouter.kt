package com.example.presentation.router

import com.example.presentation.auth.currentProfileId
import com.example.presentation.controller.ProfileController
import com.example.presentation.error.InvalidProfileAvatarException
import com.example.presentation.request.UpdateProfileRequest
import com.example.presentation.request.receiveProfileAvatar
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.profileRouter(profileController: ProfileController) {
    route("/profile") {
        get("/me") {
            val profileId = call.currentProfileId()

            call.respond(profileController.getProfile(profileId))
        }

        patch("/me") {
            val profileId = call.currentProfileId()
            val request = call.receive<UpdateProfileRequest>()

            call.respond(profileController.updateProfile(profileId, request))
        }

        get("/{userId}") {
            val profileId = call.parameters["userId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid userId")

            call.respond(profileController.getProfile(profileId))
        }

        post("/avatar") {
            val profileId = call.currentProfileId()
            val avatar = try {
                call.receiveProfileAvatar()
            } catch (exception: InvalidProfileAvatarException) {
                return@post call.respond(exception.statusCode, exception.message)
            }

            call.respond(profileController.uploadProfileAvatar(profileId, avatar))
        }
    }
}
