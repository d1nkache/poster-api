package com.example.data.repository

import com.example.data.dao.ProfileDao
import com.example.data.dao.ProfileRecord
import com.example.domain.model.AuthResult
import com.example.domain.model.ProfileEntity
import com.example.domain.model.RegisterProfile
import com.example.domain.repository.AuthRepository
import com.example.domain.service.JwtService
import com.example.domain.service.OtpSender
import com.example.domain.service.OtpService
import com.example.domain.service.PasswordHasher
import com.example.domain.service.TokenHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

class AuthRepositoryImpl(
    private val profileDao: ProfileDao,
    private val passwordHasher: PasswordHasher,
    private val otpService: OtpService,
    private val otpSender: OtpSender,
    private val jwtService: JwtService
) : AuthRepository {
    override suspend fun register(registerProfile: RegisterProfile): Boolean {
        val otp = otpService.generate()
        val created = dbQuery {
            profileDao.ensureTable()
            if (profileDao.findByEmail(registerProfile.email) != null) {
                return@dbQuery null
            }

            profileDao.createRegistered(
                registerProfile = registerProfile,
                passwordHash = passwordHasher.hash(registerProfile.password),
                otpCodeHash = otp.codeHash,
                otpExpiresAt = otp.expiresAt
            )
        } ?: return false

        otpSender.send(created.email, otp.code)

        return true
    }

    override suspend fun verifyOtp(email: String, code: String): AuthResult? = dbQuery {
        profileDao.ensureTable()
        val profile = profileDao.findByEmail(email) ?: return@dbQuery null
        if (!otpService.verify(code, profile.otpCodeHash, profile.otpExpiresAt)) {
            return@dbQuery null
        }

        profileDao.verifyEmail(profile.userId)
        createAuthResult(requireNotNull(profileDao.findByUserId(profile.userId)))
    }

    override suspend fun login(email: String, password: String): AuthResult? = dbQuery {
        profileDao.ensureTable()
        val profile = profileDao.findByEmail(email) ?: return@dbQuery null
        if (!profile.isVerified || !passwordHasher.verify(password, profile.passwordHash)) {
            return@dbQuery null
        }

        createAuthResult(profile)
    }

    override suspend fun refresh(refreshToken: String): AuthResult? = dbQuery {
        profileDao.ensureTable()
        val refreshTokenHash = TokenHasher.sha256(refreshToken)
        val profile = profileDao.findAll().firstOrNull { it.refreshTokenHash == refreshTokenHash }
            ?: return@dbQuery null

        createAuthResult(profile)
    }

    override suspend fun logout(profileId: Long): Unit = dbQuery {
        profileDao.ensureTable()
        profileDao.saveRefreshTokenHash(profileId, null)
    }

    private fun createAuthResult(profile: ProfileRecord): AuthResult {
        val tokens = jwtService.createTokens(profile.userId, profile.email)
        profileDao.saveRefreshTokenHash(profile.userId, TokenHasher.sha256(tokens.refreshToken))

        return AuthResult(
            tokens = tokens,
            profile = profile.toProfileEntity()
        )
    }

    private fun ProfileRecord.toProfileEntity(): ProfileEntity {
        return ProfileEntity(
            userId = userId,
            name = name,
            username = username,
            email = email,
            birthday = birthday,
            bio = bio,
            avatarUrl = "/media/avatars/profile-$userId-avatar.png",
            isOnline = isOnline
        )
    }

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction {
                block()
            }
        }
    }
}
