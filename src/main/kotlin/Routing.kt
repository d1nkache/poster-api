package com.example

import com.example.data.dao.ChatDao
import com.example.data.dao.ContactDao
import com.example.data.dao.ProfileDao
import com.example.data.dao.SettingsDao
import com.example.data.repository.ChatRepositoryImpl
import com.example.data.repository.ContactRepositoryImpl
import com.example.data.repository.ProfileRepositoryImpl
import com.example.data.repository.SettingsRepositoryImpl
import com.example.domain.usecase.contact.CreateContactUseCase
import com.example.domain.usecase.contact.DeleteContactUseCase
import com.example.domain.usecase.contact.GetContactUseCase
import com.example.domain.usecase.contact.GetContactsUseCase
import com.example.domain.usecase.contact.GetOrCreateContactChatUseCase
import com.example.domain.usecase.contact.UpdateContactUseCase
import com.example.domain.usecase.profile.GetProfileUseCase
import com.example.domain.usecase.profile.UpdateProfileUseCase
import com.example.domain.usecase.profile.UploadProfileAvatarUseCase
import com.example.domain.usecase.settings.DeleteMailAccessTokenUseCase
import com.example.domain.usecase.settings.GetMailAccessTokenStatusUseCase
import com.example.domain.usecase.settings.GetSettingsUseCase
import com.example.domain.usecase.settings.SaveMailAccessTokenUseCase
import com.example.domain.usecase.settings.UpdateSettingsUseCase
import com.example.presentation.controller.ContactController
import com.example.presentation.controller.ProfileController
import com.example.presentation.controller.SettingsController
import com.example.presentation.router.contactRouter
import com.example.presentation.router.profileRouter
import com.example.presentation.router.settingsRouter
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.resources.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.io.File

fun Application.configureRouting() {
    val profileDao = ProfileDao()
    val settingsDao = SettingsDao()
    val contactDao = ContactDao()
    val chatDao = ChatDao()
    val profileRepository = ProfileRepositoryImpl(profileDao = profileDao)
    val settingsRepository = SettingsRepositoryImpl(
        settingsDao = settingsDao,
        profileDao = profileDao
    )
    val contactRepository = ContactRepositoryImpl(
        contactDao = contactDao,
        profileDao = profileDao
    )
    val chatRepository = ChatRepositoryImpl(
        chatDao = chatDao,
        contactDao = contactDao,
        profileDao = profileDao
    )
    val profileController = ProfileController(
        getProfileUseCase = GetProfileUseCase(profileRepository),
        updateProfileUseCase = UpdateProfileUseCase(profileRepository),
        uploadProfileAvatarUseCase = UploadProfileAvatarUseCase(profileRepository)
    )
    val settingsController = SettingsController(
        getSettingsUseCase = GetSettingsUseCase(settingsRepository),
        updateSettingsUseCase = UpdateSettingsUseCase(settingsRepository),
        saveMailAccessTokenUseCase = SaveMailAccessTokenUseCase(settingsRepository),
        deleteMailAccessTokenUseCase = DeleteMailAccessTokenUseCase(settingsRepository),
        getMailAccessTokenStatusUseCase = GetMailAccessTokenStatusUseCase(settingsRepository)
    )
    val contactController = ContactController(
        getContactsUseCase = GetContactsUseCase(contactRepository),
        getContactUseCase = GetContactUseCase(contactRepository),
        createContactUseCase = CreateContactUseCase(contactRepository),
        updateContactUseCase = UpdateContactUseCase(contactRepository),
        deleteContactUseCase = DeleteContactUseCase(contactRepository),
        getOrCreateContactChatUseCase = GetOrCreateContactChatUseCase(chatRepository)
    )

    routing {
        staticFiles("/media", File("media"))
        profileRouter(profileController)
        settingsRouter(settingsController)
        contactRouter(contactController)

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
