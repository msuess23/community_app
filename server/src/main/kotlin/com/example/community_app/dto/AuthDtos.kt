package com.example.community_app.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDto(
  val email: String,
  val password: String,
  val displayName: String? = null
)

@Serializable
data class LoginDto(
  val email: String,
  val password: String
)

@Serializable
data class TokenResponse(
  val accessToken: String,
  val tokenType: String = "Bearer",
  val expiresIn: Long,
  val user: UserDto
)

@Serializable
data class MeDto(
  val id: Int,
  val email: String,
  val displayName: String?,
  val role: String,
  val officeId: Int?
)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(
  val email: String,
  val otp: String,
  val newPassword: String
)
