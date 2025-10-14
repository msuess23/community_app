package com.example.community_app.repository

import com.example.community_app.util.StatusScope
import com.example.community_app.model.StatusEntries
import com.example.community_app.model.StatusEntryEntity
import com.example.community_app.model.UserEntity
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

data class StatusRecord(
  val id: Int,
  val scope: StatusScope,
  val scopeId: Int,
  val statusText: String,
  val message: String?,
  val createdByUserId: Int?,
  val createdAt: Instant
)

interface StatusRepository {
  suspend fun add(scope: StatusScope, scopeId: Int, statusText: String, message: String?, createdByUserId: Int?): StatusRecord
  suspend fun list(scope: StatusScope, scopeId: Int): List<StatusRecord>
  suspend fun latest(scope: StatusScope, scopeId: Int): StatusRecord?
}

object DefaultStatusRepository : StatusRepository {

  override suspend fun add(
    scope: StatusScope,
    scopeId: Int,
    statusText: String,
    message: String?,
    createdByUserId: Int?
  ): StatusRecord = newSuspendedTransaction(Dispatchers.IO) {
    val creator = createdByUserId?.let { UserEntity.findById(it) }
    StatusEntryEntity.new {
      this.scope = scope
      this.scopeId = scopeId
      this.statusText = statusText
      this.message = message
      this.createdBy = creator
    }.toRecord()
  }

  override suspend fun list(scope: StatusScope, scopeId: Int): List<StatusRecord> =
    newSuspendedTransaction(Dispatchers.IO) {
      StatusEntryEntity.find { (StatusEntries.scope eq scope) and (StatusEntries.scopeId eq scopeId) }
        .orderBy(StatusEntries.createdAt to SortOrder.DESC)
        .map { it.toRecord() }
    }

  override suspend fun latest(scope: StatusScope, scopeId: Int): StatusRecord? =
    newSuspendedTransaction(Dispatchers.IO) {
      StatusEntryEntity.find { (StatusEntries.scope eq scope) and (StatusEntries.scopeId eq scopeId) }
        .orderBy(StatusEntries.createdAt to SortOrder.DESC)
        .limit(1)
        .firstOrNull()
        ?.toRecord()
    }

  private fun StatusEntryEntity.toRecord() = StatusRecord(
    id = this.id.value,
    scope = this.scope,
    scopeId = this.scopeId,
    statusText = this.statusText,
    message = this.message,
    createdByUserId = this.createdBy?.id?.value,
    createdAt = this.createdAt
  )
}
