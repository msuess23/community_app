package com.example.community_app.repository

import com.example.community_app.model.UserEntity
import com.example.community_app.model.Users
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {
  fun findByEmail(email: String) = transaction {
    UserEntity.find { Users.email eq email }.firstOrNull()
  }

  fun findById(id: Int) = transaction {
    UserEntity.findById(id)
  }

  fun create(email: String, displayName: String?, passwordHash: String) = transaction {
    UserEntity.new {
      this.email = email
      this.displayName = displayName
      this.passwordHash = passwordHash
    }
  }

  fun updatePassword(id: Int, newHash: String) = transaction {
    UserEntity.findById(id)?.let { it.passwordHash = newHash }
  }
}
