package com.example.community_app.dto

import com.example.community_app.util.Role
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
  val id: Int,
  val email: String,
  val displayName: String?,
  val role: Role,
  val officeId: Int? = null
)

@Serializable
data class UserUpdateDto(
  val displayName: String
)

@Serializable
data class SettingsDto(
  val language: String,
  val theme: String,
  val notificationsEnabled: Boolean,
  val syncEnabled: Boolean
)

@Serializable
data class SettingsUpdateDto(
  val language: String? = null,
  val theme: String? = null,
  val notificationsEnabled: Boolean? = null,
  val syncEnabled: Boolean? = null
)
