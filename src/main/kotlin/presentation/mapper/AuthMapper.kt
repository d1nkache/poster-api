package com.example.presentation.mapper

import com.example.domain.model.AuthResult
import com.example.presentation.response.AuthResponse

object AuthMapper {
    fun asAuthResponse(authResult: AuthResult): AuthResponse {
        return AuthResponse(
            accessToken = authResult.tokens.accessToken,
            refreshToken = authResult.tokens.refreshToken,
            profile = ProfileMapper.asProfileResponse(authResult.profile)
        )
    }
}
