package com.example.community_app.geocoding.domain.usecase

import com.example.community_app.geocoding.domain.Address
import com.example.community_app.geocoding.domain.AddressRepository
import kotlinx.coroutines.flow.Flow

class GetAddressHistoryUseCase(
  private val repository: AddressRepository
) {
  operator fun invoke(): Flow<List<Address>> {
    return repository.getHistory()
  }
}