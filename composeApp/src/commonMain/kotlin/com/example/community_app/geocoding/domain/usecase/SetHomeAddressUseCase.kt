package com.example.community_app.geocoding.domain.usecase

import com.example.community_app.geocoding.domain.model.Address
import com.example.community_app.geocoding.domain.repository.AddressRepository


class SetHomeAddressUseCase(
  private val repository: AddressRepository
) {
  suspend operator fun invoke(address: Address) {
    repository.setHomeAddress(address)
  }
}