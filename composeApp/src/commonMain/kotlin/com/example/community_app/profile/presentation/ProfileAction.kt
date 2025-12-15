package com.example.community_app.profile.presentation

sealed interface ProfileAction {
  data object OnToggleEditMode : ProfileAction
  data class OnDisplayNameChange(val name: String) : ProfileAction
  data object OnSaveProfile : ProfileAction

  // Log in/out
  data object OnLoginClick : ProfileAction
  data object OnLogoutClick : ProfileAction
  data object OnLogoutConfirm : ProfileAction
  data object OnLogoutCancel : ProfileAction

  // Change password
  data object OnChangePasswordClick : ProfileAction
  data object OnChangePasswordDismiss : ProfileAction
  data object OnChangePasswordConfirm : ProfileAction
}