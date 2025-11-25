package com.example.community_app.auth.data.network

import com.example.community_app.core.data.safeCall
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.ForgotPasswordRequest
import com.example.community_app.dto.LoginDto
import com.example.community_app.dto.RegisterDto
import com.example.community_app.dto.ResetPasswordRequest
import com.example.community_app.dto.TokenResponse
import com.example.community_app.util.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class KtorRemoteAuthDataSource(
  private val httpClient: HttpClient
) : RemoteAuthDataSource {
  override suspend fun login(request: LoginDto): Result<TokenResponse, DataError.Remote> {
    return safeCall {
      httpClient.post("$BASE_URL/api/auth/login") {
        setBody(request)
      }
    }
  }

  override suspend fun register(request: RegisterDto): Result<TokenResponse, DataError.Remote> {
    return safeCall {
      httpClient.post("$BASE_URL/api/auth/register") {
        setBody(request)
      }
    }
  }

  override suspend fun forgotPassword(email: String): Result<Unit, DataError.Remote> {
    return safeCall {
      httpClient.post("$BASE_URL/api/auth/forgot-password") {
        setBody(ForgotPasswordRequest(email))
      }
    }
  }

  override suspend fun resetPassword(request: ResetPasswordRequest): Result<TokenResponse, DataError.Remote> {
    return safeCall {
      httpClient.post("$BASE_URL/api/auth/reset-password") {
        setBody(request)
      }
    }
  }
}