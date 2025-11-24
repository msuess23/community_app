package com.example.community_app.auth.data.network

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.LoginDto
import com.example.community_app.dto.RegisterDto
import com.example.community_app.dto.ResetPasswordRequest
import com.example.community_app.dto.TokenResponse

interface RemoteAuthDataSource {
  suspend fun login(request: LoginDto): Result<TokenResponse, DataError.Remote>
  suspend fun register(request: RegisterDto): Result<TokenResponse, DataError.Remote>
  suspend fun forgotPassword(email: String): Result<Unit, DataError.Remote>
  suspend fun resetPassword(request: ResetPasswordRequest): Result<TokenResponse, DataError.Remote>
}