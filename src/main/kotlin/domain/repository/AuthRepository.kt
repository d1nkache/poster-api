package com.example.domain.repository

import com.example.domain.model.AuthResult
import com.example.domain.model.RegisterProfile

interface AuthRepository {
    suspend fun register(registerProfile: RegisterProfile): Boolean

    suspend fun verifyOtp(email: String, code: String): AuthResult?

    suspend fun login(email: String, password: String): AuthResult?

    suspend fun refresh(refreshToken: String): AuthResult?

    suspend fun logout(profileId: Long)
}
