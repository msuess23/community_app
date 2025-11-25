package com.example.community_app.auth.domain

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.LoginDto
import com.example.community_app.dto.RegisterDto
import com.example.community_app.dto.ResetPasswordRequest
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
  val authState: Flow<AuthState>

  suspend fun login(loginDto: LoginDto): Result<Unit, DataError.Remote>
  suspend fun register(registerDto: RegisterDto): Result<Unit, DataError.Remote>
  suspend fun logout()

  suspend fun forgotPassword(email: String): Result<Unit, DataError.Remote>
  suspend fun resetPassword(request: ResetPasswordRequest): Result<Unit, DataError.Remote>

  suspend fun getAccessToken(): String?
}