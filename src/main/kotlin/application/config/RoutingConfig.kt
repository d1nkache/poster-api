package com.example.application.config

import com.example.application.di.AppDependencies
import com.example.presentation.resource.Articles
import com.example.presentation.router.authRouter
import com.example.presentation.router.contactRouter
import com.example.presentation.router.messageRouter
import com.example.presentation.router.profileRouter
import com.example.presentation.router.settingsRouter
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticFiles
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import java.io.File

fun Application.installRouting(dependencies: AppDependencies = AppDependencies()) {
    routing {
        staticFiles("/media", File("media"))
        authRouter(dependencies.authController)
        authenticate("auth-jwt") {
            profileRouter(dependencies.profileController)
            settingsRouter(dependencies.settingsController)
            contactRouter(dependencies.contactController)
            messageRouter(dependencies.messageController)
        }

        get("/") {
            call.respondText("Hello, World!")
        }
        get<Articles> { article ->
            call.respond("List of articles sorted starting from ${article.sort}")
        }
        webSocket("/ws") {
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
