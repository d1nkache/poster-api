package com.example.domain.service

interface OtpSender {
    suspend fun send(email: String, code: String)
}
