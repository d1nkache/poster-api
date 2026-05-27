package com.example.domain.usecase.auth

import com.example.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(profileId: Long) {
        authRepository.logout(profileId)
    }
}
