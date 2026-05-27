package com.example.domain.service

interface OtpSender {
    suspend fun send(email: String, code: String)
}

class LoggingOtpSender : OtpSender {
    override suspend fun send(email: String, code: String) {
        println("OTP for $email: $code")
    }
}
