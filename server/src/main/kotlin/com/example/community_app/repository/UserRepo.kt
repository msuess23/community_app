package com.example.community_app.repository

import com.example.community_app.model.UserEntity
import com.example.community_app.model.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Read model used outside the DB layer to avoid Exposed DAO lifecycle issues.
 */
data class UserRecord(
  val id: Int,
  val email: String,
  val displayName: String?,
  val passwordHash: String
)

interface UserRepository {
  suspend fun findByEmail(email: String): UserRecord?
  suspend fun findById(id: Int): UserRecord?
  suspend fun create(email: String, passwordHash: String, displayName: String?): UserRecord
  suspend fun updatePassword(id: Int, newHash: String)
  suspend fun deleteById(id: Int)
}

object DefaultUserRepository : UserRepository {

  override suspend fun findByEmail(email: String): UserRecord? = newSuspendedTransaction(Dispatchers.IO) {
    UserEntity.find { Users.email eq email }
      .limit(1)
      .firstOrNull()
      ?.let { it.toRecord() }
  }

  override suspend fun findById(id: Int): UserRecord? = newSuspendedTransaction(Dispatchers.IO) {
    UserEntity.findById(id)?.toRecord()
  }

  override suspend fun create(email: String, passwordHash: String, displayName: String?): UserRecord =
    newSuspendedTransaction(Dispatchers.IO) {
      val entity = UserEntity.new {
        this.email = email
        this.passwordHash = passwordHash
        this.displayName = displayName
      }
      entity.toRecord()
    }

  override suspend fun updatePassword(id: Int, newHash: String) {
    newSuspendedTransaction(Dispatchers.IO) {
      UserEntity.findById(id)?.apply { this.passwordHash = newHash }
    }
  }

  override suspend fun deleteById(id: Int) {
    newSuspendedTransaction(Dispatchers.IO) {
      Users.deleteWhere { Users.id eq id }
    }
  }

  // --- mapping helper (must be called inside a transaction) ---
  private fun UserEntity.toRecord() = UserRecord(
    id = this.id.value,
    email = this.email,
    displayName = this.displayName,
    passwordHash = this.passwordHash
  )
}
