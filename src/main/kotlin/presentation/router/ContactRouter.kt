package com.example.presentation.router

import com.example.presentation.auth.currentProfileId
import com.example.presentation.controller.ContactController
import com.example.presentation.error.badRequest
import com.example.presentation.error.notFound
import com.example.presentation.request.CreateContactRequest
import com.example.presentation.request.UpdateContactRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.contactRouter(contactController: ContactController) {
    route("/contacts") {
        get {
            val profileId = call.currentProfileId()
            val query = call.request.queryParameters["q"]

            call.respond(contactController.getContacts(profileId, query))
        }

        get("/{contactId}") {
            val profileId = call.currentProfileId()
            val contactId = call.parameters["contactId"]?.toLongOrNull()
                ?: badRequest("INVALID_CONTACT_ID", "Invalid contactId")
            val contact = contactController.getContact(profileId, contactId)
                ?: notFound("CONTACT_NOT_FOUND", "Contact not found")

            call.respond(contact)
        }

        post {
            val profileId = call.currentProfileId()
            val request = call.receive<CreateContactRequest>()

            call.respond(HttpStatusCode.Created, contactController.createContact(profileId, request))
        }

        patch("/{contactId}") {
            val profileId = call.currentProfileId()
            val contactId = call.parameters["contactId"]?.toLongOrNull()
                ?: badRequest("INVALID_CONTACT_ID", "Invalid contactId")
            val request = call.receive<UpdateContactRequest>()
            val contact = contactController.updateContact(profileId, contactId, request)
                ?: notFound("CONTACT_NOT_FOUND", "Contact not found")

            call.respond(contact)
        }

        delete("/{contactId}") {
            val profileId = call.currentProfileId()
            val contactId = call.parameters["contactId"]?.toLongOrNull()
                ?: badRequest("INVALID_CONTACT_ID", "Invalid contactId")

            if (!contactController.deleteContact(profileId, contactId)) {
                notFound("CONTACT_NOT_FOUND", "Contact not found")
            }

            call.respond(HttpStatusCode.NoContent)
        }

        post("/{contactId}/chat") {
            val profileId = call.currentProfileId()
            val contactId = call.parameters["contactId"]?.toLongOrNull()
                ?: badRequest("INVALID_CONTACT_ID", "Invalid contactId")
            val chat = contactController.getOrCreateChat(profileId, contactId)
                ?: notFound("CONTACT_NOT_FOUND", "Contact not found")

            call.respond(chat)
        }
    }
}
