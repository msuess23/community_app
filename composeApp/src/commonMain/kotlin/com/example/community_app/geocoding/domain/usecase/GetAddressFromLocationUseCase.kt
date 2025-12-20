package com.example.community_app.geocoding.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.geocoding.domain.Address
import com.example.community_app.geocoding.domain.AddressRepository

class GetAddressFromLocationUseCase(
  private val repository: AddressRepository
) {
  suspend operator fun invoke(lat: Double, lon: Double): Result<Address?, DataError.Remote> {
    return repository.getAddressFromCoordinates(lat, lon)
  }
}