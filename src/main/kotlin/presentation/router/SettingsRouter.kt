package com.example.presentation.router

import com.example.presentation.auth.currentUserId
import com.example.presentation.controller.SettingsController
import com.example.presentation.request.SaveMailAccessTokenRequest
import com.example.presentation.request.UpdateSettingsRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.settingsRouter(settingsController: SettingsController) {
    route("/settings") {
        get {
            val userId = call.currentUserId()

            call.respond(settingsController.getSettings(userId))
        }

        patch {
            val userId = call.currentUserId()
            val request = call.receive<UpdateSettingsRequest>()

            call.respond(settingsController.updateSettings(userId, request))
        }

        put("/mail-access-token") {
            val userId = call.currentUserId()
            val request = call.receive<SaveMailAccessTokenRequest>()

            settingsController.saveMailAccessToken(userId, request)
            call.respond(HttpStatusCode.NoContent)
        }

        delete("/mail-access-token") {
            val userId = call.currentUserId()

            settingsController.deleteMailAccessToken(userId)
            call.respond(HttpStatusCode.NoContent)
        }

        get("/mail-access-token/status") {
            val userId = call.currentUserId()

            call.respond(settingsController.getMailAccessTokenStatus(userId))
        }
    }
}
