package com.example.community_app.profile.presentation

import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.geocoding.domain.Address
import com.example.community_app.geocoding.presentation.AddressSuggestion
import com.example.community_app.profile.domain.User

data class ProfileState(
  val isLoading: Boolean = false,
  val isSaving: Boolean = false,
  val errorMessage: UiText? = null,

  val user: User? = null,
  val editName: String = "",
  val isEditing: Boolean = false,

  val homeAddress: Address? = null,
  val addressSearchQuery: String = "",
  val isAddressSearchActive: Boolean = false,
  val addressSuggestions: List<AddressSuggestion> = emptyList(),
  val currentLocation: Location? = null,

  val showLogoutDialog: Boolean = false,
  val showPasswordResetDialog: Boolean = false,

  val isLogoutClearDataChecked: Boolean = false
) {
  val email: String get() = user?.email ?: ""
}