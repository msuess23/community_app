package com.example.community_app.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.AuthState
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.profile.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository
) : ViewModel() {

  private val _state = MutableStateFlow(ProfileState())
  val state = _state
    .onStart {
      observeUser()
      refreshUser()
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000L),
      ProfileState()
    )

  fun onAction(action: ProfileAction) {
    when (action) {
      ProfileAction.OnToggleEditMode -> {
        _state.update {
          it.copy(
            isEditing = !it.isEditing,
            displayName = it.displayName
          )
        }
      }
      is ProfileAction.OnDisplayNameChange -> {
        _state.update { it.copy(displayName = action.name) }
      }
      ProfileAction.OnSaveProfile -> saveProfile()

      // Logout
      ProfileAction.OnLogoutClick -> _state.update { it.copy(showLogoutDialog = true) }
      ProfileAction.OnLogoutCancel -> _state.update { it.copy(showLogoutDialog = false) }
      ProfileAction.OnLogoutConfirm -> performLogout()

      // Password Reset
      ProfileAction.OnChangePasswordClick -> performPasswordResetTrigger()
      ProfileAction.OnChangePasswordDismiss -> _state.update { it.copy(showPasswordResetDialog = false) }
      ProfileAction.OnChangePasswordConfirm -> _state.update { it.copy(showPasswordResetDialog = false) }

      else -> Unit
    }
  }

  private fun observeUser() {
    userRepository.getUser().onEach { user ->
      if (user != null) {
        _state.update { it.copy(
          email = user.email,
          displayName = user.displayName ?: ""
        ) }
      }
    }.launchIn(viewModelScope)
  }

  private fun refreshUser() {
    viewModelScope.launch {
      userRepository.refreshUser()
    }
  }

  private fun saveProfile() {
    viewModelScope.launch {
      _state.update { it.copy(isSaving = true) }
      val newName = _state.value.displayName

      val result = userRepository.updateDisplayName(newName)

      if (result is Result.Success) {
        _state.update { it.copy(isSaving = false, isEditing = false) }
      } else {
        _state.update { it.copy(
          isSaving = false,
          errorMessage = (result as Result.Error).error.toUiText()
        ) }
      }
    }
  }

  private fun performLogout() {
    viewModelScope.launch {
      _state.update { it.copy(showLogoutDialog = false, isLoading = true) }
      authRepository.logout()
    }
  }

  private fun performPasswordResetTrigger() {
    val email = _state.value.email.ifBlank { return }
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }
      authRepository.forgotPassword(email)
        .onSuccess {
          _state.update { it.copy(isLoading = false, showPasswordResetDialog = true) }
        }
        .onError {
          _state.update { it.copy(isLoading = false) }
        }
    }
  }
}