package com.example.community_app.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp

object Users : IntIdTable("users") {
  val email = varchar("email", 255).uniqueIndex()
  val displayName = varchar("display_name", 255).nullable()
  val passwordHash = varchar("password_hash", 255)
  val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<UserEntity>(Users)

  var email by Users.email
  var displayName by Users.displayName
  var passwordHash by Users.passwordHash
  var createdAt by Users.createdAt
}
