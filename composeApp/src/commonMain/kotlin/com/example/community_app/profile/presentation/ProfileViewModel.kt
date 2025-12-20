package com.example.community_app.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.auth.domain.usecase.RequestPasswordResetUseCase
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.geocoding.domain.Address
import com.example.community_app.geocoding.domain.usecase.AddToAddressHistoryUseCase
import com.example.community_app.geocoding.domain.usecase.GetAddressHistoryUseCase
import com.example.community_app.geocoding.domain.usecase.SearchAddressUseCase
import com.example.community_app.geocoding.domain.usecase.SetHomeAddressUseCase
import com.example.community_app.profile.domain.usecase.GetProfileDataUseCase
import com.example.community_app.profile.domain.usecase.LogoutUserUseCase
import com.example.community_app.profile.domain.usecase.UpdateUserProfileUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ProfileViewModel(
  private val getProfileData: GetProfileDataUseCase,
  private val updateUserProfile: UpdateUserProfileUseCase,
  private val logoutUser: LogoutUserUseCase,
  private val requestPasswordReset: RequestPasswordResetUseCase,
  private val setHomeAddress: SetHomeAddressUseCase,
  private val searchAddress: SearchAddressUseCase,
  private val getAddressHistory: GetAddressHistoryUseCase,
  private val addToAddressHistory: AddToAddressHistoryUseCase
) : ViewModel() {
  private val _state = MutableStateFlow(ProfileState())
  val state = _state
    .onStart {
      loadInitialData()
      observeSearchQuery()
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000L),
      ProfileState()
    )

  fun onAction(action: ProfileAction) {
    when (action) {
      ProfileAction.OnToggleEditMode -> {
        val user = _state.value.user ?: return
        _state.update { it.copy(isEditing = !it.isEditing, editName = user.displayName ?: "") }
      }
      is ProfileAction.OnDisplayNameChange -> _state.update { it.copy(editName = action.name) }
      ProfileAction.OnSaveProfile -> saveProfile()

      is ProfileAction.OnAddressSearchActiveChange -> {
        _state.update {
          it.copy(
            isAddressSearchActive = action.active,
            addressSearchQuery = if (!action.active) "" else it.addressSearchQuery
          )
        }
      }
      is ProfileAction.OnAddressQueryChange -> {
        _state.update { it.copy(addressSearchQuery = action.query) }
      }
      is ProfileAction.OnSelectAddress -> selectAddress(action.address)

      // Logout
      ProfileAction.OnLogoutClick -> _state.update { it.copy(showLogoutDialog = true) }
      ProfileAction.OnLogoutCancel -> _state.update { it.copy(showLogoutDialog = false) }
      ProfileAction.OnLogoutConfirm -> performLogout()
      is ProfileAction.OnLogoutClearDataChange -> _state.update {
        it.copy(isLogoutClearDataChecked = action.checked)
      }

      // Password Reset
      ProfileAction.OnChangePasswordClick -> triggerPasswordReset()
      ProfileAction.OnChangePasswordDismiss -> _state.update { it.copy(showPasswordResetDialog = false) }
      ProfileAction.OnChangePasswordConfirm -> _state.update { it.copy(showPasswordResetDialog = false) }
      else -> Unit
    }
  }

  private fun loadInitialData() {
    getProfileData()
      .onEach { result ->
        when (result) {
          is Result.Success -> {
            val data = result.data
            _state.update { current ->
              current.copy(
                user = data.user,
                homeAddress = data.homeAddress,
                editName = if (!current.isEditing) {
                  data.user?.displayName ?: ""
                } else current.editName,
                errorMessage = data.syncError?.toUiText() ?: current.errorMessage
              )
            }
          }
          is Result.Error -> {
            _state.update { it.copy(errorMessage = result.error.toUiText()) }
          }
        }
      }
      .launchIn(viewModelScope)
  }

  private fun observeSearchQuery() {
    combine(
      _state.map { it.addressSearchQuery }.distinctUntilChanged(),
      getAddressHistory()
    ) { query, history -> Pair(query, history) }
      .debounce(500L)
      .map { (query, history) ->
        if (query.isBlank()) {
          history
        } else {
          val localMatches = history.filter {
            it.getUiLine1().contains(query, ignoreCase = true) ||
                it.city?.contains(query, ignoreCase = true) == true
          }
          val apiResult = searchAddress(query)
          val apiMatches = if (apiResult is Result.Success) apiResult.data else emptyList()
          (localMatches + apiMatches).distinctBy { "${it.latitude},${it.longitude}" }
        }
      }
      .onEach { suggestions ->
        _state.update { it.copy(addressSugestions = suggestions) }
      }
      .launchIn(viewModelScope)
  }

  private fun selectAddress(address: Address) {
    viewModelScope.launch {
      _state.update {
        it.copy(
          homeAddress = address,
          isAddressSearchActive = false,
          addressSearchQuery = ""
        )
      }
      setHomeAddress(address)
    }
  }

  private fun saveProfile() {
    viewModelScope.launch {
      val newName = _state.value.editName
      _state.update { it.copy(isSaving = true) }

      val result = updateUserProfile(newName)

      _state.update { it.copy(isSaving = false) }

      if (result is Result.Success) {
        _state.update { it.copy(isEditing = false) }
      } else {
        _state.update {
          it.copy(errorMessage = (result as Result.Error).error.toUiText())
        }
      }
    }
  }

  private fun performLogout() {
    viewModelScope.launch {
      val shouldClearData = _state.value.isLogoutClearDataChecked

      _state.update { it.copy(showLogoutDialog = false, isLoading = true) }

      logoutUser(clearData = shouldClearData)

      _state.update { it.copy(isLoading = false) }
    }
  }

  private fun triggerPasswordReset() {
    val email = _state.value.email
    if (email.isBlank()) return

    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }

      val result = requestPasswordReset(email)

      _state.update { it.copy(isLoading = false) }

      if (result is Result.Success) {
        _state.update { it.copy(showPasswordResetDialog = true) }
      } else {
        _state.update { it.copy(errorMessage = (result as Result.Error).error.toUiText()) }
      }
    }
  }
}