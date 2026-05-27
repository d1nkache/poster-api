package com.example.presentation.router

import com.example.presentation.auth.currentUserId
import com.example.presentation.controller.MessageController
import com.example.presentation.error.badRequest
import com.example.presentation.error.notFound
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
        val profileId = call.currentUserId()
        val chatId = call.parameters["chatId"]?.toLongOrNull()
            ?: badRequest("INVALID_CHAT_ID", "Invalid chatId")
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 50
        val offset = call.request.queryParameters["offset"]?.toLongOrNull()?.takeIf { it >= 0 } ?: 0
        val messages = messageController.getChatMessages(profileId, chatId, limit, offset)
            ?: notFound("CHAT_NOT_FOUND", "Chat not found")

        call.respond(messages)
    }

    post("/chats/{chatId}/messages") {
        val profileId = call.currentUserId()
        val chatId = call.parameters["chatId"]?.toLongOrNull()
            ?: badRequest("INVALID_CHAT_ID", "Invalid chatId")
        val request = call.receive<SendMessageRequest>()
        val message = messageController.sendMessage(profileId, chatId, request)
            ?: notFound("CHAT_NOT_FOUND", "Chat not found")

        call.respond(HttpStatusCode.Created, message)
    }

    get("/messages/{messageId}") {
        val profileId = call.currentUserId()
        val messageId = call.parameters["messageId"]?.toLongOrNull()
            ?: badRequest("INVALID_MESSAGE_ID", "Invalid messageId")
        val message = messageController.getMessage(profileId, messageId)
            ?: notFound("MESSAGE_NOT_FOUND", "Message not found")

        call.respond(message)
    }

    patch("/messages/{messageId}/read") {
        val profileId = call.currentUserId()
        val messageId = call.parameters["messageId"]?.toLongOrNull()
            ?: badRequest("INVALID_MESSAGE_ID", "Invalid messageId")

        if (!messageController.markMessageAsRead(profileId, messageId)) {
            notFound("MESSAGE_NOT_FOUND", "Message not found")
        }

        call.respond(HttpStatusCode.NoContent)
    }

    patch("/chats/{chatId}/messages/read") {
        val profileId = call.currentUserId()
        val chatId = call.parameters["chatId"]?.toLongOrNull()
            ?: badRequest("INVALID_CHAT_ID", "Invalid chatId")

        if (!messageController.markChatMessagesAsRead(profileId, chatId)) {
            notFound("CHAT_NOT_FOUND", "Chat not found")
        }

        call.respond(HttpStatusCode.NoContent)
    }

    delete("/messages/{messageId}") {
        val profileId = call.currentUserId()
        val messageId = call.parameters["messageId"]?.toLongOrNull()
            ?: badRequest("INVALID_MESSAGE_ID", "Invalid messageId")

        if (!messageController.deleteMessage(profileId, messageId)) {
            notFound("MESSAGE_NOT_FOUND", "Message not found")
        }

        call.respond(HttpStatusCode.NoContent)
    }
}
