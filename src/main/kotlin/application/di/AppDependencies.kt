package com.example.application.di

import com.example.data.dao.ChatDao
import com.example.data.dao.ContactDao
import com.example.data.dao.MailOutboxDao
import com.example.data.dao.MessageDao
import com.example.data.dao.OtpOutboxDao
import com.example.data.dao.ProfileDao
import com.example.data.dao.SettingsDao
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.ChatRepositoryImpl
import com.example.data.repository.ContactRepositoryImpl
import com.example.data.repository.MessageRepositoryImpl
import com.example.data.repository.ProfileRepositoryImpl
import com.example.data.repository.SettingsRepositoryImpl
import com.example.data.service.QueuedOtpSender
import com.example.domain.service.JwtService
import com.example.domain.service.OtpService
import com.example.domain.service.PasswordHasher
import com.example.domain.usecase.auth.LoginUseCase
import com.example.domain.usecase.auth.LogoutUseCase
import com.example.domain.usecase.auth.RefreshTokenUseCase
import com.example.domain.usecase.auth.RegisterUseCase
import com.example.domain.usecase.auth.VerifyOtpUseCase
import com.example.domain.usecase.contact.CreateContactUseCase
import com.example.domain.usecase.contact.DeleteContactUseCase
import com.example.domain.usecase.contact.GetContactUseCase
import com.example.domain.usecase.contact.GetContactsUseCase
import com.example.domain.usecase.contact.GetOrCreateContactChatUseCase
import com.example.domain.usecase.contact.UpdateContactUseCase
import com.example.domain.usecase.message.DeleteMessageUseCase
import com.example.domain.usecase.message.GetChatMessagesUseCase
import com.example.domain.usecase.message.GetMessageUseCase
import com.example.domain.usecase.message.MarkChatMessagesAsReadUseCase
import com.example.domain.usecase.message.MarkMessageAsReadUseCase
import com.example.domain.usecase.message.SendMessageUseCase
import com.example.domain.usecase.profile.GetProfileUseCase
import com.example.domain.usecase.profile.UpdateProfileUseCase
import com.example.domain.usecase.profile.UploadProfileAvatarUseCase
import com.example.domain.usecase.settings.DeleteMailAccessTokenUseCase
import com.example.domain.usecase.settings.GetMailAccessTokenStatusUseCase
import com.example.domain.usecase.settings.GetSettingsUseCase
import com.example.domain.usecase.settings.SaveMailAccessTokenUseCase
import com.example.domain.usecase.settings.UpdateSettingsUseCase
import com.example.presentation.controller.AuthController
import com.example.presentation.controller.ContactController
import com.example.presentation.controller.MessageController
import com.example.presentation.controller.ProfileController
import com.example.presentation.controller.SettingsController

class AppDependencies {
    private val profileDao = ProfileDao()
    private val settingsDao = SettingsDao()
    private val contactDao = ContactDao()
    private val chatDao = ChatDao()
    private val messageDao = MessageDao()
    private val mailOutboxDao = MailOutboxDao()
    private val otpOutboxDao = OtpOutboxDao()
    private val passwordHasher = PasswordHasher()
    private val jwtService = JwtService()

    private val authRepository = AuthRepositoryImpl(
        profileDao = profileDao,
        passwordHasher = passwordHasher,
        otpService = OtpService(passwordHasher),
        otpSender = QueuedOtpSender(otpOutboxDao),
        jwtService = jwtService
    )
    private val profileRepository = ProfileRepositoryImpl(profileDao = profileDao)
    private val settingsRepository = SettingsRepositoryImpl(
        settingsDao = settingsDao,
        profileDao = profileDao
    )
    private val contactRepository = ContactRepositoryImpl(
        contactDao = contactDao,
        profileDao = profileDao
    )
    private val chatRepository = ChatRepositoryImpl(
        chatDao = chatDao,
        contactDao = contactDao,
        profileDao = profileDao
    )
    private val messageRepository = MessageRepositoryImpl(
        messageDao = messageDao,
        mailOutboxDao = mailOutboxDao,
        chatDao = chatDao,
        contactDao = contactDao
    )

    val authController = AuthController(
        registerUseCase = RegisterUseCase(authRepository),
        verifyOtpUseCase = VerifyOtpUseCase(authRepository),
        loginUseCase = LoginUseCase(authRepository),
        refreshTokenUseCase = RefreshTokenUseCase(authRepository),
        logoutUseCase = LogoutUseCase(authRepository)
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
    val messageController = MessageController(
        getChatMessagesUseCase = GetChatMessagesUseCase(messageRepository),
        sendMessageUseCase = SendMessageUseCase(messageRepository),
        getMessageUseCase = GetMessageUseCase(messageRepository),
        markMessageAsReadUseCase = MarkMessageAsReadUseCase(messageRepository),
        markChatMessagesAsReadUseCase = MarkChatMessagesAsReadUseCase(messageRepository),
        deleteMessageUseCase = DeleteMessageUseCase(messageRepository)
    )
}
