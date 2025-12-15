package com.example.community_app.profile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

const val CURRENT_USER_ID = 0

@Entity(tableName = "user")
data class UserEntity(
  @PrimaryKey(autoGenerate = false)
  val id: Int = CURRENT_USER_ID,
  val userId: Int,
  val email: String,
  val displayName: String?,
)