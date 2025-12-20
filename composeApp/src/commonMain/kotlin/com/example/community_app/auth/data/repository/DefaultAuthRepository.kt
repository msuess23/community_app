package com.example.community_app.auth.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.community_app.auth.data.network.RemoteAuthDataSource
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.AuthState
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.LoginDto
import com.example.community_app.dto.RegisterDto
import com.example.community_app.dto.ResetPasswordRequest
import com.example.community_app.dto.TokenResponse
import com.example.community_app.profile.data.local.UserDao
import com.example.community_app.profile.data.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultAuthRepository(
  private val remoteAuthDataSource: RemoteAuthDataSource,
  private val userDao: UserDao,
  private val dataStore: DataStore<Preferences>
) : AuthRepository {
  private val KEY_ACCESS_TOKEN = stringPreferencesKey("auth_access_token")

  override val authState: Flow<AuthState> = dataStore.data.map { prefs ->
    val token = prefs[KEY_ACCESS_TOKEN]

    if (token != null) {
      try {
        AuthState.Authenticated
      } catch (e: Exception) {
        AuthState.Unauthenticated
      }
    } else {
      AuthState.Unauthenticated
    }
  }

  override suspend fun login(loginDto: LoginDto): Result<Unit, DataError.Remote> {
    return handleAuthResult(remoteAuthDataSource.login(loginDto))
  }

  override suspend fun register(registerDto: RegisterDto): Result<Unit, DataError.Remote> {
    return handleAuthResult(remoteAuthDataSource.register(registerDto))
  }

  override suspend fun logout() {
    remoteAuthDataSource.logout()
    clearSession()
  }

  override suspend fun forgotPassword(email: String): Result<Unit, DataError.Remote> {
    return remoteAuthDataSource.forgotPassword(email)
  }

  override suspend fun resetPassword(request: ResetPasswordRequest): Result<Unit, DataError.Remote> {
    return handleAuthResult(remoteAuthDataSource.resetPassword(request))
  }

  override suspend fun getAccessToken(): String? {
    return dataStore.data.first()[KEY_ACCESS_TOKEN]
  }

  private suspend fun handleAuthResult(
    result: Result<TokenResponse, DataError.Remote>
  ): Result<Unit, DataError.Remote> {
    return when (result) {
      is Result.Success -> {
        saveSession(result.data.accessToken)

        try {
          userDao.upsertUser(result.data.user.toEntity())
        } catch (e: Exception) {
          e.printStackTrace()
        }

        Result.Success(Unit)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  private suspend fun saveSession(token: String) {
    dataStore.edit { prefs ->
      prefs[KEY_ACCESS_TOKEN] = token
    }
  }

  private suspend fun clearSession() {
    dataStore.edit { prefs ->
      prefs.remove(KEY_ACCESS_TOKEN)
    }
  }
}