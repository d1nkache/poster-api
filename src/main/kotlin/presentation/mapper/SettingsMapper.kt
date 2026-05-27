package com.example.presentation.mapper

import com.example.domain.model.SettingsEntity
import com.example.presentation.response.MailAccessTokenStatusResponse
import com.example.presentation.response.SettingsResponse

object SettingsMapper {
    fun asSettingsResponse(settings: SettingsEntity): SettingsResponse {
        return SettingsResponse(
            language = settings.language,
            hasMailAccessToken = settings.hasMailAccessToken,
            smtpHost = settings.smtpHost,
            smtpPort = settings.smtpPort,
            imapHost = settings.imapHost,
            imapPort = settings.imapPort
        )
    }

    fun asMailAccessTokenStatusResponse(configured: Boolean): MailAccessTokenStatusResponse {
        return MailAccessTokenStatusResponse(configured = configured)
    }
}
