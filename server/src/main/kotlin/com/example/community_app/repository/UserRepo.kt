package com.example.community_app.repository

import com.example.community_app.model.UserEntity
import com.example.community_app.model.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object UserRepository {
  suspend fun findByEmail(email: String): UserEntity? = newSuspendedTransaction(Dispatchers.IO) {
    UserEntity.find { Users.email eq email }.firstOrNull()
  }

  suspend fun findById(id: Int): UserEntity? = newSuspendedTransaction(Dispatchers.IO) {
    UserEntity.findById(id)
  }

  suspend fun create(email: String, passwordHash: String, displayName: String?): UserEntity = newSuspendedTransaction(Dispatchers.IO) {
    UserEntity.new {
      this.email = email
      this.passwordHash = passwordHash
      this.displayName = displayName
    }
  }
}
