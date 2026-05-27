package com.example.presentation.router

import com.example.presentation.auth.currentUserId
import com.example.presentation.controller.ProfileController
import com.example.presentation.error.badRequest
import com.example.presentation.request.UpdateProfileRequest
import com.example.presentation.request.receiveProfileAvatar
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
            val profileId = call.currentUserId()

            call.respond(profileController.getProfile(profileId))
        }

        patch("/me") {
            val profileId = call.currentUserId()
            val request = call.receive<UpdateProfileRequest>()

            call.respond(profileController.updateProfile(profileId, request))
        }

        get("/{userId}") {
            val profileId = call.parameters["userId"]?.toProfileId()
                ?: badRequest("INVALID_USER_ID", "Invalid userId")

            call.respond(profileController.getProfile(profileId))
        }

        post("/avatar") {
            val profileId = call.currentUserId()
            val avatar = call.receiveProfileAvatar()

            call.respond(profileController.uploadProfileAvatar(profileId, avatar))
        }
    }
}

private fun String.toProfileId(): Long? {
    return removePrefix("user-").toLongOrNull()
}
