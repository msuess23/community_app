package com.example.community_app.profile.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Upsert
  suspend fun upsertUser(user: UserEntity)

  @Query("SELECT * FROM user")
  fun getUser(): Flow<UserEntity?>

  @Query("DELETE FROM user")
  suspend fun clearUser()
}