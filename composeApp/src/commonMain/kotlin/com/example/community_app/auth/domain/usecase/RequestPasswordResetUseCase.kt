package com.example.community_app.auth.domain.usecase

import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result

class RequestPasswordResetUseCase(
  private val authRepository: AuthRepository
) {
  suspend operator fun invoke(email: String): Result<Unit, DataError.Remote> {
    if (email.isBlank()) {
      return Result.Error(DataError.Remote.UNKNOWN)
    }
    return authRepository.forgotPassword(email)
  }
}