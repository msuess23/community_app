package com.example.community_app.profile.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.profile.domain.UserRepository

class UpdateUserProfileUseCase(
  private val userRepository: UserRepository
) {
  suspend operator fun invoke(displayName: String): Result<Unit, DataError.Remote> {
    val result = userRepository.updateDisplayName(displayName)

    return if (result is Result.Success) {
      Result.Success(Unit)
    } else {
      Result.Error((result as Result.Error).error)
    }
  }
}