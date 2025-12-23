package com.example.community_app.geocoding.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.geocoding.domain.model.Address
import com.example.community_app.geocoding.domain.repository.AddressRepository

class SearchAddressUseCase(
  private val repository: AddressRepository
) {
  suspend operator fun invoke(query: String): Result<List<Address>, DataError.Remote> {
    if (query.isBlank()) return Result.Success(emptyList())
    return repository.searchOnline(query)
  }
}