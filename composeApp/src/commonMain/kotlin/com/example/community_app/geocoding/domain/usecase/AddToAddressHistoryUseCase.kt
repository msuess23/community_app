package com.example.community_app.geocoding.domain.usecase

import com.example.community_app.geocoding.domain.Address
import com.example.community_app.geocoding.domain.AddressRepository

class AddToAddressHistoryUseCase(
  private val repository: AddressRepository
) {
  suspend operator fun invoke(address: Address) {
    repository.addToHistory(address)
  }
}