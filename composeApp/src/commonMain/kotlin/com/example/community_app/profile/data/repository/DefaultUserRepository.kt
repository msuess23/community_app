package com.example.community_app.profile.data.repository

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.UserUpdateDto
import com.example.community_app.profile.data.local.UserDao
import com.example.community_app.profile.data.mappers.toUser
import com.example.community_app.profile.data.mappers.toEntity
import com.example.community_app.profile.data.network.RemoteUserDataSource
import com.example.community_app.profile.domain.model.User
import com.example.community_app.profile.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultUserRepository(
  private val remoteUserDataSource: RemoteUserDataSource,
  private val userDao: UserDao
) : UserRepository {
  override fun getUser(): Flow<User?> {
    return userDao.getUser().map { it?.toUser() }
  }

  override suspend fun refreshUser(): Result<User, DataError.Remote> {
    return when (val result = remoteUserDataSource.getProfile()) {
      is Result.Success -> {
        userDao.upsertUser(result.data.toEntity())

        val refreshedUser = userDao.getUser().first()?.toUser()
          ?: result.data.toEntity().toUser()
        Result.Success(refreshedUser)
      }
      is Result.Error -> {
        Result.Error(result.error)
      }
    }
  }

  override suspend fun updateDisplayName(name: String): Result<User, DataError.Remote> {
    val request = UserUpdateDto(name)

    return when(val result = remoteUserDataSource.updateProfile(request)) {
      is Result.Success -> {
        userDao.upsertUser(result.data.toEntity())

        val updatedUser = userDao.getUser().first()?.toUser()
          ?: result.data.toEntity().toUser()
        Result.Success(updatedUser)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun clearUser() {
    userDao.clearUser()
  }
}