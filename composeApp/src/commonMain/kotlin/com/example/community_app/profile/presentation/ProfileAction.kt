package com.example.community_app.profile.presentation

import com.example.community_app.geocoding.domain.Address
import com.example.community_app.office.domain.Office
import com.example.community_app.ticket.presentation.ticket_edit.TicketEditAction

sealed interface ProfileAction {
  data object OnToggleEditMode : ProfileAction
  data class OnDisplayNameChange(val name: String) : ProfileAction
  data object OnSaveProfile : ProfileAction

  data class OnAddressQueryChange(val query: String) : ProfileAction
  data class OnAddressSearchActiveChange(val active: Boolean) : ProfileAction
  data class OnSelectAddress(val address: Address) : ProfileAction
  data object OnUseCurrentLocationClick : ProfileAction

  // Log in/out
  data object OnLoginClick : ProfileAction

  data object OnLogoutClick : ProfileAction
  data object OnLogoutConfirm : ProfileAction
  data object OnLogoutCancel : ProfileAction
  data class OnLogoutClearDataChange(val checked: Boolean) : ProfileAction

  // Change password
  data object OnChangePasswordClick : ProfileAction
  data object OnChangePasswordDismiss : ProfileAction
  data object OnChangePasswordConfirm : ProfileAction
}