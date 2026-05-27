package com.example.presentation.request

import com.example.domain.model.SendMessage
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val bodyText: String
)

fun SendMessageRequest.toSendMessage(): SendMessage {
    return SendMessage(bodyText = bodyText)
}
