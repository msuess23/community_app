package com.example.community_app.geocoding.domain.repository

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.geocoding.domain.model.Address

interface GeocodingRepository {
  suspend fun searchAddress(query: String): Result<List<Address>, DataError.Remote>
  suspend fun getAddressFromCoordinates(lat: Double, lon: Double): Result<Address?, DataError.Remote>
}