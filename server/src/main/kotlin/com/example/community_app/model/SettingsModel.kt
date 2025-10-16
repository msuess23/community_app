package com.example.community_app.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Settings : IntIdTable(name = "SETTINGS") {
  val user = reference("USER_ID", Users, onDelete = ReferenceOption.CASCADE).uniqueIndex()
  val language = varchar("LANGUAGE", 16).default("en")
  val theme = varchar("THEME", 16).default("light") // "light" | "dark"
  val notificationsEnabled = bool("NOTIFICATIONS_ENABLED").default(false)
  val syncEnabled = bool("SYNC_ENABLED").default(false)
  val createdAt = timestamp("CREATED_AT").defaultExpression(CurrentTimestamp)
  val updatedAt = timestamp("UPDATED_AT").defaultExpression(CurrentTimestamp)
}

class SettingsEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<SettingsEntity>(Settings)
  var user by UserEntity referencedOn Settings.user
  var language by Settings.language
  var theme by Settings.theme
  var notificationsEnabled by Settings.notificationsEnabled
  var syncEnabled by Settings.syncEnabled
  var createdAt by Settings.createdAt
  var updatedAt by Settings.updatedAt
}
