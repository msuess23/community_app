package com.example.community_app.profile.domain.usecase

import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.geocoding.domain.AddressRepository
import com.example.community_app.profile.domain.UserRepository

class LogoutUserUseCase(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository,
  private val addressRepository: AddressRepository
) {
  suspend operator fun invoke(clearData: Boolean) {
    if (clearData) {
      addressRepository.clearAllForUser()
    }

    userRepository.clearUser()
    authRepository.logout()
  }
}