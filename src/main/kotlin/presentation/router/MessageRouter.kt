package com.example.presentation.router

import com.example.presentation.auth.currentProfileId
import com.example.presentation.controller.MessageController
import com.example.presentation.request.SendMessageRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

fun Route.messageRouter(messageController: MessageController) {
    get("/chats/{chatId}/messages") {
        val profileId = call.currentProfileId()
        val chatId = call.parameters["chatId"]?.toLongOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid chatId")
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 50
        val offset = call.request.queryParameters["offset"]?.toLongOrNull()?.takeIf { it >= 0 } ?: 0
        val messages = messageController.getChatMessages(profileId, chatId, limit, offset)
            ?: return@get call.respond(HttpStatusCode.NotFound, "Chat not found")

        call.respond(messages)
    }

    post("/chats/{chatId}/messages") {
        val profileId = call.currentProfileId()
        val chatId = call.parameters["chatId"]?.toLongOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid chatId")
        val request = call.receive<SendMessageRequest>()
        val message = messageController.sendMessage(profileId, chatId, request)
            ?: return@post call.respond(HttpStatusCode.NotFound, "Chat not found")

        call.respond(HttpStatusCode.Created, message)
    }

    get("/messages/{messageId}") {
        val profileId = call.currentProfileId()
        val messageId = call.parameters["messageId"]?.toLongOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid messageId")
        val message = messageController.getMessage(profileId, messageId)
            ?: return@get call.respond(HttpStatusCode.NotFound, "Message not found")

        call.respond(message)
    }

    patch("/messages/{messageId}/read") {
        val profileId = call.currentProfileId()
        val messageId = call.parameters["messageId"]?.toLongOrNull()
            ?: return@patch call.respond(HttpStatusCode.BadRequest, "Invalid messageId")

        if (!messageController.markMessageAsRead(profileId, messageId)) {
            return@patch call.respond(HttpStatusCode.NotFound, "Message not found")
        }

        call.respond(HttpStatusCode.NoContent)
    }

    patch("/chats/{chatId}/messages/read") {
        val profileId = call.currentProfileId()
        val chatId = call.parameters["chatId"]?.toLongOrNull()
            ?: return@patch call.respond(HttpStatusCode.BadRequest, "Invalid chatId")

        if (!messageController.markChatMessagesAsRead(profileId, chatId)) {
            return@patch call.respond(HttpStatusCode.NotFound, "Chat not found")
        }

        call.respond(HttpStatusCode.NoContent)
    }

    delete("/messages/{messageId}") {
        val profileId = call.currentProfileId()
        val messageId = call.parameters["messageId"]?.toLongOrNull()
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid messageId")

        if (!messageController.deleteMessage(profileId, messageId)) {
            return@delete call.respond(HttpStatusCode.NotFound, "Message not found")
        }

        call.respond(HttpStatusCode.NoContent)
    }
}
