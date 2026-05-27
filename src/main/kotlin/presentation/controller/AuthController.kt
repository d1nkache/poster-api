package com.example.presentation.controller

import com.example.domain.model.RegisterProfile
import com.example.domain.usecase.auth.LoginUseCase
import com.example.domain.usecase.auth.LogoutUseCase
import com.example.domain.usecase.auth.RefreshTokenUseCase
import com.example.domain.usecase.auth.RegisterUseCase
import com.example.domain.usecase.auth.VerifyOtpUseCase
import com.example.presentation.mapper.AuthMapper
import com.example.presentation.request.LoginRequest
import com.example.presentation.request.RefreshRequest
import com.example.presentation.request.RegisterRequest
import com.example.presentation.request.VerifyOtpRequest
import com.example.presentation.response.AuthResponse

class AuthController(
    private val registerUseCase: RegisterUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase
) {
    suspend fun register(request: RegisterRequest): Boolean {
        return registerUseCase(
            RegisterProfile(
                email = request.email,
                password = request.password,
                displayName = request.displayName
            )
        )
    }

    suspend fun verifyOtp(request: VerifyOtpRequest): AuthResponse? {
        return verifyOtpUseCase(request.email, request.code)?.let(AuthMapper::asAuthResponse)
    }

    suspend fun login(request: LoginRequest): AuthResponse? {
        return loginUseCase(request.email, request.password)?.let(AuthMapper::asAuthResponse)
    }

    suspend fun refresh(request: RefreshRequest): AuthResponse? {
        return refreshTokenUseCase(request.refreshToken)?.let(AuthMapper::asAuthResponse)
    }

    suspend fun logout(profileId: Long) {
        logoutUseCase(profileId)
    }
}
