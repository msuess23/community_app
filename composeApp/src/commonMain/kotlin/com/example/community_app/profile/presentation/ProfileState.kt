package com.example.community_app.profile.presentation

import com.example.community_app.core.presentation.helpers.UiText

data class ProfileState(
  val isLoading: Boolean = false,
  val isEditing: Boolean = false,
  val isSaving: Boolean = false,

  val email: String = "",
  val displayName: String = "",

  val showLogoutDialog: Boolean = false,
  val showPasswordResetDialog: Boolean = false,
  val errorMessage: UiText? = null
)