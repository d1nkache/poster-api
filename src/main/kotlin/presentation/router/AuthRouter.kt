package com.example.presentation.router

import com.example.presentation.auth.currentUserId
import com.example.presentation.controller.AuthController
import com.example.presentation.error.conflict
import com.example.presentation.error.badRequest
import com.example.presentation.error.unauthorized
import com.example.presentation.request.LoginRequest
import com.example.presentation.request.RefreshRequest
import com.example.presentation.request.RegisterRequest
import com.example.presentation.request.VerifyOtpRequest
import com.example.presentation.response.RegisterResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRouter(authController: AuthController) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val registered = authController.register(request)
            if (!registered) {
                conflict("EMAIL_ALREADY_REGISTERED", "Email already registered")
            }

            call.respond(HttpStatusCode.Created, RegisterResponse(message = "OTP code sent"))
        }

        post("/verify-otp") {
            val request = call.receive<VerifyOtpRequest>()
            val response = authController.verifyOtp(request)
                ?: badRequest("INVALID_OTP", "Invalid OTP")

            call.respond(response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authController.login(request)
                ?: unauthorized("INVALID_CREDENTIALS", "Invalid credentials or email is not verified")

            call.respond(response)
        }

        post("/refresh") {
            val request = call.receive<RefreshRequest>()
            val response = authController.refresh(request)
                ?: unauthorized("INVALID_REFRESH_TOKEN", "Invalid refresh token")

            call.respond(response)
        }

        authenticate("auth-jwt") {
            post("/logout") {
                authController.logout(call.currentUserId())

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
