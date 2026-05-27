package com.example.domain.usecase.auth

import com.example.domain.model.AuthResult
import com.example.domain.repository.AuthRepository

class RefreshTokenUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(refreshToken: String): AuthResult? {
        return authRepository.refresh(refreshToken)
    }
}
