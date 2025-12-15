package com.example.community_app.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.appointment.domain.usecase.ScheduleAppointmentRemindersUseCase
import com.example.community_app.core.domain.permission.AppPermissionService
import com.example.community_app.core.domain.permission.CalendarPermissionService
import com.example.community_app.core.domain.permission.PermissionStatus
import com.example.community_app.core.util.restartApp
import com.example.community_app.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val settingsRepository: SettingsRepository,
  private val permissionService: AppPermissionService,
  private val calendarPermissionService: CalendarPermissionService,
  private val scheduleReminders: ScheduleAppointmentRemindersUseCase
) : ViewModel() {
  private val _state = MutableStateFlow(SettingsState())

  private var pendingSyncEnable = false

  val state = _state
    .onStart { observeSettings() }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000L),
      _state.value
    )

  fun onAction(action: SettingsAction) {
    when (action) {
      is SettingsAction.OnTabChange -> {
        _state.update { it.copy(selectedTabIndex = action.index) }
      }

      // Theme
      is SettingsAction.OnThemeChange -> {
        viewModelScope.launch {
          settingsRepository.setTheme(action.theme)
        }
      }

      // Language
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

      // Calendar Sync
      is SettingsAction.OnToggleCalendarSync -> {
        handleCalendarSyncToggle(action.enabled)
      }
      is SettingsAction.OnResume -> {
        checkRealPermissionStatus()
      }
      is SettingsAction.OnOpenSettings -> {
        pendingSyncEnable = true
        calendarPermissionService.openAppSettings()
        _state.update { it.copy(showCalendarPermissionRationale = false) }
      }

      // Notifications
      is SettingsAction.OnToggleNotifications -> {
        viewModelScope.launch {
          if (action.enabled) {
            val granted = permissionService.requestNotificationPermission()
            settingsRepository.setNotificationsEnabled(granted)
          } else {
            settingsRepository.setNotificationsEnabled(false)
          }
        }
      }
      is SettingsAction.OnToggleNotifyTickets -> {
        viewModelScope.launch { settingsRepository.setNotifyTickets(action.enabled) }
      }
      is SettingsAction.OnToggleNotifyInfos -> {
        viewModelScope.launch { settingsRepository.setNotifyInfos(action.enabled) }
      }
      is SettingsAction.OnToggleNotifyAppointments -> {
        viewModelScope.launch { settingsRepository.setNotifyAppointments(action.enabled) }
      }
      is SettingsAction.OnChangeAppointmentReminderOffset -> {
        viewModelScope.launch {
          settingsRepository.setAppointmentReminderOffset(action.minutes)
          scheduleReminders()
        }
      }
    }
  }

  private fun observeSettings() {
    settingsRepository.settings.onEach { appSettings ->
        _state.update { it.copy(settings = appSettings) }
    }.launchIn(viewModelScope)
  }

  private fun handleCalendarSyncToggle(enabled: Boolean) {
    viewModelScope.launch {
      _state.update { it.copy(showCalendarPermissionRationale = false) }

      if (!enabled) {
        pendingSyncEnable = false
        settingsRepository.setCalendarSyncEnabled(false)
      } else {
        val status = calendarPermissionService.checkPermission()

        when (status) {
          PermissionStatus.GRANTED -> {
            settingsRepository.setCalendarSyncEnabled(true)
            pendingSyncEnable = false
          }
          PermissionStatus.DENIED, PermissionStatus.NOT_DETERMINED -> {
            pendingSyncEnable = true

            val newStatus = calendarPermissionService.requestPermission()
            if (newStatus == PermissionStatus.GRANTED) {
              settingsRepository.setCalendarSyncEnabled(true)
              pendingSyncEnable = false
            } else {
              _state.update { it.copy(showCalendarPermissionRationale = true) }
            }
          }
          PermissionStatus.DENIED_ALWAYS -> {
            pendingSyncEnable = true
            _state.update { it.copy(showCalendarPermissionRationale = true) }
          }
        }
      }
    }
  }

  private fun checkRealPermissionStatus() {
    viewModelScope.launch {
      val currentSettings = settingsRepository.settings.first()
      val status = calendarPermissionService.checkPermission()

      if (currentSettings.calendarSyncEnabled && status != PermissionStatus.GRANTED) {
        settingsRepository.setCalendarSyncEnabled(false)
        pendingSyncEnable = false
      }

      if (!currentSettings.calendarSyncEnabled && pendingSyncEnable && status == PermissionStatus.GRANTED) {
        settingsRepository.setCalendarSyncEnabled(true)
        pendingSyncEnable = false
      }
    }
  }
}