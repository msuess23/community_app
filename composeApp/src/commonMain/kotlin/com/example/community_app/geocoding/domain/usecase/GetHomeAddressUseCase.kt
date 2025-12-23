package com.example.community_app.geocoding.domain.usecase

import com.example.community_app.geocoding.domain.model.Address
import com.example.community_app.geocoding.domain.repository.AddressRepository
import kotlinx.coroutines.flow.Flow

class GetHomeAddressUseCase(
  private val repository: AddressRepository
) {
  operator fun invoke(): Flow<Address?> {
    return repository.getHomeAddress()
  }
}