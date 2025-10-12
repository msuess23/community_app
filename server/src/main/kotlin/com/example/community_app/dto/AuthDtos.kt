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
  val tokenType: String = "Bearer"
)

@Serializable
data class MeDto(
  val id: Int,
  val email: String,
  val displayName: String?
)

@Serializable
data class ChangePasswordDto(
  val oldPassword: String,
  val newPassword: String
)
