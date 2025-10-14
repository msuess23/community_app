package com.example.community_app.model

import com.example.community_app.util.StatusScope
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object StatusEntries : IntIdTable(name = "STATUS_ENTRIES") {
  val scope = enumerationByName("SCOPE", 16, StatusScope::class)
  val scopeId = integer("SCOPE_ID")
  val statusText = varchar("STATUS_TEXT", 64)
  val message = text("MESSAGE").nullable()
  val createdBy = reference("CREATED_BY", Users).nullable()
  val createdAt = timestamp("CREATED_AT").defaultExpression(CurrentTimestamp)

  init {
    index(false, scope, scopeId)
    index(false, scope, scopeId, createdAt)
  }
}

class StatusEntryEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<StatusEntryEntity>(StatusEntries)
  var scope by StatusEntries.scope
  var scopeId by StatusEntries.scopeId
  var statusText by StatusEntries.statusText
  var message by StatusEntries.message
  var createdBy by UserEntity optionalReferencedOn StatusEntries.createdBy
  var createdAt by StatusEntries.createdAt
}
