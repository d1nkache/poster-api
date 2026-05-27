package com.example.domain.usecase.auth

import com.example.domain.model.AuthResult
import com.example.domain.repository.AuthRepository

class VerifyOtpUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): AuthResult? {
        return authRepository.verifyOtp(email, code)
    }
}
