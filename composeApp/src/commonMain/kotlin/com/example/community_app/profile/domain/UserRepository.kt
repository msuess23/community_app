package com.example.community_app.profile.domain

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface UserRepository {
  fun getUser(): Flow<User?>

  suspend fun refreshUser(): Result<User, DataError.Remote>

  suspend fun updateDisplayName(name: String): Result<User, DataError.Remote>

  suspend fun clearUser()
}