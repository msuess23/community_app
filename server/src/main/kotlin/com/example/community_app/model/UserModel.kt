package com.example.community_app.model

import com.example.community_app.util.Role
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : IntIdTable(name = "USERS") {
  val email = varchar("EMAIL", 255).uniqueIndex()
  val displayName = varchar("DISPLAY_NAME", 255).nullable()
  val passwordHash = varchar("PASSWORD_HASH", 255)
  val role = enumerationByName("ROLE", length = 16, klass = Role::class)
  val officeId = integer("OFFICE_ID").nullable() // (Dev) kein FK, bis Offices existieren
  val createdAt = timestamp("CREATED_AT").defaultExpression(CurrentTimestamp)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<UserEntity>(Users)
  var email by Users.email
  var displayName by Users.displayName
  var passwordHash by Users.passwordHash
  var role by Users.role
  var officeId by Users.officeId
  var createdAt by Users.createdAt
}
