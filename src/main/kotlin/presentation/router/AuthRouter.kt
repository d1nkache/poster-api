package com.example.presentation.router

import com.example.presentation.auth.currentProfileId
import com.example.presentation.controller.AuthController
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
                return@post call.respond(HttpStatusCode.Conflict, "Email already registered")
            }

            call.respond(HttpStatusCode.Created, RegisterResponse(message = "OTP code sent"))
        }

        post("/verify-otp") {
            val request = call.receive<VerifyOtpRequest>()
            val response = authController.verifyOtp(request)
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid OTP")

            call.respond(response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authController.login(request)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials or email is not verified")

            call.respond(response)
        }

        post("/refresh") {
            val request = call.receive<RefreshRequest>()
            val response = authController.refresh(request)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid refresh token")

            call.respond(response)
        }

        authenticate("auth-jwt") {
            post("/logout") {
                authController.logout(call.currentProfileId())

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
