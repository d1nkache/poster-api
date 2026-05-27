package com.example.domain.usecase.auth

import com.example.domain.model.AuthResult
import com.example.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult? {
        return authRepository.login(email, password)
    }
}
