package com.example.community_app.repository

import com.example.community_app.util.Role
import com.example.community_app.model.UserEntity
import com.example.community_app.model.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

data class UserRecord(
  val id: Int,
  val email: String,
  val displayName: String?,
  val passwordHash: String,
  val role: Role,
  val officeId: Int?
)

interface UserRepository {
  suspend fun findByEmail(email: String): UserRecord?
  suspend fun findById(id: Int): UserRecord?
  suspend fun create(email: String, passwordHash: String, displayName: String?, role: Role, officeId: Int?): UserRecord
  suspend fun updatePassword(id: Int, newHash: String)
  suspend fun updateProfile(id: Int, displayName: String): UserRecord?
  suspend fun deleteById(id: Int)
}

object DefaultUserRepository : UserRepository {

  override suspend fun findByEmail(email: String): UserRecord? = newSuspendedTransaction(Dispatchers.IO) {
    UserEntity.find { Users.email eq email }
      .limit(1)
      .firstOrNull()
      ?.toRecord()
  }

  override suspend fun findById(id: Int): UserRecord? = newSuspendedTransaction(Dispatchers.IO) {
    UserEntity.findById(id)?.toRecord()
  }

  override suspend fun create(
    email: String,
    passwordHash: String,
    displayName: String?,
    role: Role,
    officeId: Int?
  ): UserRecord = newSuspendedTransaction(Dispatchers.IO) {
    UserEntity.new {
      this.email = email
      this.passwordHash = passwordHash
      this.displayName = displayName
      this.role = role
      this.officeId = officeId
    }.toRecord()
  }

  override suspend fun updatePassword(id: Int, newHash: String) {
    newSuspendedTransaction(Dispatchers.IO) {
      UserEntity.findById(id)?.apply { this.passwordHash = newHash }
    }
  }

  override suspend fun updateProfile(id: Int, displayName: String): UserRecord? =
    newSuspendedTransaction(Dispatchers.IO) {
      UserEntity.findById(id)?.apply { this.displayName = displayName }?.toRecord()
    }

  override suspend fun deleteById(id: Int) {
    newSuspendedTransaction(Dispatchers.IO) {
      Users.deleteWhere { Users.id eq id }
    }
  }

  private fun UserEntity.toRecord() = UserRecord(
    id = this.id.value,
    email = this.email,
    displayName = this.displayName,
    passwordHash = this.passwordHash,
    role = this.role,
    officeId = this.officeId
  )
}
