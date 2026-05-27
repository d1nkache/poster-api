package com.example.domain.usecase.auth

import com.example.domain.model.RegisterProfile
import com.example.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(registerProfile: RegisterProfile): Boolean {
        return authRepository.register(registerProfile)
    }
}
