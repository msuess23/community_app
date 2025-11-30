package com.example.community_app.auth.domain.usecase

import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IsUserLoggedInUseCase(
  private val authRepository: AuthRepository
) {
  operator fun invoke(): Flow<Boolean> {
    return authRepository.authState.map { it is AuthState.Authenticated }
  }
}