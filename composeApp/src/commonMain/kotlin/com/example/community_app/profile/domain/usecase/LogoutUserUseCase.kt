package com.example.community_app.profile.domain.usecase

import com.example.community_app.auth.domain.repository.AuthRepository
import com.example.community_app.geocoding.domain.repository.AddressRepository
import com.example.community_app.profile.domain.repository.UserRepository
import com.example.community_app.ticket.domain.repository.TicketRepository

class LogoutUserUseCase(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository,
  private val addressRepository: AddressRepository,
  private val ticketRepository: TicketRepository
) {
  suspend operator fun invoke(clearData: Boolean) {
    if (clearData) {
      addressRepository.clearAllForUser()
      ticketRepository.clearUserData()
    }

    userRepository.clearUser()
    authRepository.logout()
  }
}