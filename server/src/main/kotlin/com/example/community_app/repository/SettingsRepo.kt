package com.example.community_app.repository

import com.example.community_app.model.Settings
import com.example.community_app.model.SettingsEntity
import com.example.community_app.model.UserEntity
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

data class SettingsRecord(
  val language: String,
  val theme: String,
  val notificationsEnabled: Boolean,
  val syncEnabled: Boolean
)

interface SettingsRepository {
  suspend fun getByUserId(userId: Int): SettingsRecord?
  suspend fun upsert(userId: Int, patch: SettingsRecord): SettingsRecord
  suspend fun deleteForUser(userId: Int)
}

object DefaultSettingsRepository : SettingsRepository {
  override suspend fun getByUserId(userId: Int): SettingsRecord? =
    newSuspendedTransaction(Dispatchers.IO) {
      SettingsEntity.find { Settings.user eq userId }.limit(1).firstOrNull()?.toRecord()
    }

  override suspend fun upsert(userId: Int, patch: SettingsRecord): SettingsRecord =
    newSuspendedTransaction(Dispatchers.IO) {
      val existing = SettingsEntity.find { Settings.user eq userId }.limit(1).firstOrNull()
      if (existing != null) {
        Settings.update({ Settings.user eq userId }) {
          it[language] = patch.language
          it[theme] = patch.theme
          it[notificationsEnabled] = patch.notificationsEnabled
          it[syncEnabled] = patch.syncEnabled
          it[updatedAt] = java.time.Instant.now()
        }
        SettingsEntity.find { Settings.user eq userId }.limit(1).first().toRecord()
      } else {
        SettingsEntity.new {
          this.user = UserEntity[userId]
          this.language = patch.language
          this.theme = patch.theme
          this.notificationsEnabled = patch.notificationsEnabled
          this.syncEnabled = patch.syncEnabled
        }.toRecord()
      }
    }

  override suspend fun deleteForUser(userId: Int) {
    newSuspendedTransaction(Dispatchers.IO) {
      SettingsEntity.find { Settings.user eq userId }.forEach { it.delete() }
    }
  }

  private fun SettingsEntity.toRecord() = SettingsRecord(
    language = this.language,
    theme = this.theme,
    notificationsEnabled = this.notificationsEnabled,
    syncEnabled = this.syncEnabled
  )
}
