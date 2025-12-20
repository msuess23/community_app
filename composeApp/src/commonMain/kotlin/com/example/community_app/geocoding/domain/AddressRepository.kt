package com.example.community_app.geocoding.domain

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface AddressRepository {
  suspend fun searchOnline(query: String): Result<List<Address>, DataError.Remote>
  suspend fun getAddressFromCoordinates(lat: Double, lon: Double): Result<Address?, DataError.Remote>

  fun getHistory(): Flow<List<Address>>
  suspend fun addToHistory(address: Address)

  fun getHomeAddress(): Flow<Address?>
  suspend fun setHomeAddress(address: Address)

  suspend fun clearAllForUser()
}