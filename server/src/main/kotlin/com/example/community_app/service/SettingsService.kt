package com.example.community_app.service

import com.example.community_app.dto.SettingsDto
import com.example.community_app.dto.SettingsUpdateDto
import com.example.community_app.repository.DefaultSettingsRepository
import com.example.community_app.repository.SettingsRecord
import com.example.community_app.repository.SettingsRepository
import io.ktor.server.auth.jwt.*

class SettingsService(
  private val repo: SettingsRepository
) {
  suspend fun get(principal: JWTPrincipal): SettingsDto? {
    val userId = principal.subject!!.toInt()
    return repo.getByUserId(userId)?.toDto()
  }

  suspend fun put(principal: JWTPrincipal, patch: SettingsUpdateDto): SettingsDto {
    val userId = principal.subject!!.toInt()
    val merged = mergeDefaults(repo.getByUserId(userId), patch)
    return repo.upsert(userId, merged).toDto()
  }

  suspend fun delete(principal: JWTPrincipal) {
    val userId = principal.subject!!.toInt()
    repo.deleteForUser(userId)
  }

  private fun mergeDefaults(current: SettingsRecord?, patch: SettingsUpdateDto): SettingsRecord {
    val language = patch.language ?: current?.language ?: "en"
    val theme = patch.theme ?: current?.theme ?: "light"
    val notif = patch.notificationsEnabled ?: current?.notificationsEnabled ?: false
    val sync = patch.syncEnabled ?: current?.syncEnabled ?: false
    return SettingsRecord(language, theme, notif, sync)
  }

  private fun SettingsRecord.toDto() = SettingsDto(
    language = language,
    theme = theme,
    notificationsEnabled = notificationsEnabled,
    syncEnabled = syncEnabled
  )

  companion object {
    fun default(): SettingsService = SettingsService(DefaultSettingsRepository)
  }
}
