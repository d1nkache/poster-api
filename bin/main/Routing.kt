package com.example

import com.example.data.repository.ProfileRepositoryStub
import com.example.domain.usecase.profile.GetProfileUseCase
import com.example.domain.usecase.profile.UpdateProfileUseCase
import com.example.domain.usecase.profile.UploadProfileAvatarUseCase
import com.example.presentation.controller.ProfileController
import com.example.presentation.router.profileRouter
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.resources.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Application.configureRouting() {
    val profileRepository = ProfileRepositoryStub()
    val profileController = ProfileController(
        getProfileUseCase = GetProfileUseCase(profileRepository),
        updateProfileUseCase = UpdateProfileUseCase(profileRepository),
        uploadProfileAvatarUseCase = UploadProfileAvatarUseCase(profileRepository)
    )

    routing {
        profileRouter(profileController)

        get("/") {
            call.respondText("Hello, World!")
        }
        get<Articles> { article ->
            call.respond("List of articles sorted starting from ${article.sort}")
        }
        webSocket("/ws") { // websocketSession
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
