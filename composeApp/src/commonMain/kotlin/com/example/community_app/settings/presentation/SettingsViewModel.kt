package com.example.community_app.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.AuthState
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.util.restartApp
import com.example.community_app.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val settingsRepository: SettingsRepository,
  private val authRepository: AuthRepository
) : ViewModel() {
  private val _state = MutableStateFlow(SettingsState())

  val state = _state
    .onStart {
      observeSettings()
      observeAuth()
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000L),
      _state.value
    )

  fun onAction(action: SettingsAction) {
    when (action) {
      is SettingsAction.OnThemeChange -> {
        viewModelScope.launch {
          settingsRepository.setTheme(action.theme)
        }
      }
      is SettingsAction.OnLanguageSelect -> {
        if (action.language != state.value.settings.language) {
          _state.update { it.copy(pendingLanguage = action.language) }
        }
      }
      is SettingsAction.OnLanguageDismiss -> {
        _state.update { it.copy(pendingLanguage = null) }
      }
      is SettingsAction.OnLanguageConfirm -> {
        val newLang = _state.value.pendingLanguage ?: return
        viewModelScope.launch {
          settingsRepository.setLanguage(newLang)
          _state.update { it.copy(pendingLanguage = null) }
          restartApp()
        }
      }
      is SettingsAction.OnLogoutClick -> {
        _state.update { it.copy(showLogoutDialog = true) }
      }
      is SettingsAction.OnLogoutCancel -> {
        _state.update { it.copy(showLogoutDialog = false) }
      }
      is SettingsAction.OnLogoutConfirm -> {
        performLogout()
      }
      is SettingsAction.OnChangePasswordClick -> {
        performPasswordResetTrigger()
      }
      is SettingsAction.OnChangePasswordDismiss -> {
        _state.update { it.copy(showPasswordResetDialog = false) }
      }
    }
  }

  private fun observeAuth() {
    authRepository.authState.onEach { authState ->
      if (authState is AuthState.Authenticated) {
        _state.update { it.copy(currentUserEmail = authState.user.email) }
      } else {
        _state.update { it.copy(currentUserEmail = null) }
      }
    }.launchIn(viewModelScope)
  }

  private fun observeSettings() {
    settingsRepository.settings.onEach { appSettings ->
        _state.update { it.copy(settings = appSettings) }
    }.launchIn(viewModelScope)
  }

  private fun performLogout() {
    viewModelScope.launch {
      _state.update { it.copy(
        showLogoutDialog = false,
        isLoading = true
      ) }

      authRepository.logout()
      _state.update { it.copy(isLoading = false) }
    }
  }

  private fun performPasswordResetTrigger() {
    val email = _state.value.currentUserEmail ?: return

    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }

      authRepository.forgotPassword(email)
        .onSuccess {
          _state.update { it.copy(
            isLoading = false,
            showPasswordResetDialog = true
          ) }
        }
        .onError {
          _state.update { it.copy(isLoading = false) }
        }
    }
  }
}