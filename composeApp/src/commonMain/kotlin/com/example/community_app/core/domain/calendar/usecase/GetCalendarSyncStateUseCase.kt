package com.example.community_app.core.domain.calendar.usecase

import com.example.community_app.core.domain.permission.CalendarPermissionService
import com.example.community_app.core.domain.permission.PermissionStatus
import com.example.community_app.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.first

data class CalendarSyncState(
  val hasPermission: Boolean,
  val shouldAutoAdd: Boolean
)

class GetCalendarSyncStateUseCase(
  private val settingsRepository: SettingsRepository,
  private val calendarPermissionService: CalendarPermissionService
) {
  suspend operator fun invoke(): CalendarSyncState {
    val permission = calendarPermissionService.checkPermission()
    val hasPermission = permission == PermissionStatus.GRANTED

    val settings = settingsRepository.settings.first()

    return CalendarSyncState(
      hasPermission = hasPermission,
      shouldAutoAdd = hasPermission && settings.calendarSyncEnabled
    )
  }
}