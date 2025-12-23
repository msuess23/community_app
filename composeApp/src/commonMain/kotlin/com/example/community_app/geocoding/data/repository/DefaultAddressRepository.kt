package com.example.community_app.geocoding.data.repository

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.geocoding.data.local.AddressDao
import com.example.community_app.geocoding.data.mappers.toAddress
import com.example.community_app.geocoding.data.mappers.toEntity
import com.example.community_app.geocoding.data.network.RemoteGeocodingDataSource
import com.example.community_app.geocoding.domain.model.Address
import com.example.community_app.geocoding.domain.repository.AddressRepository
import com.example.community_app.profile.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class DefaultAddressRepository(
  private val remoteDataSource: RemoteGeocodingDataSource,
  private val addressDao: AddressDao,
  private val userRepository: UserRepository
) : AddressRepository {
  override suspend fun searchOnline(query: String): Result<List<Address>, DataError.Remote> {
    return when (val result = remoteDataSource.search(query)) {
      is Result.Success -> {
        val mapped = result.data.features.map {
          val props = it.properties
          Address(
            formatted = props.formatted,
            street = props.street,
            houseNumber = props.housenumber,
            zipCode = props.postcode,
            city = props.city,
            country = props.country,
            latitude = props.lat,
            longitude = props.lon
          )
        }
        Result.Success(mapped)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun getAddressFromCoordinates(lat: Double, lon: Double): Result<Address?, DataError.Remote> {
    return when (val result = remoteDataSource.reverse(lat, lon)) {
      is Result.Success -> {
        val props = result.data.features.firstOrNull()?.properties
        val mapped = props?.let {
          Address(
            formatted = it.formatted,
            street = it.street,
            houseNumber = it.housenumber,
            zipCode = it.postcode,
            city = it.city,
            country = it.country,
            latitude = it.lat,
            longitude = it.lon
          )
        }
        Result.Success(mapped)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun getHistory(): Flow<List<Address>> {
    return userRepository.getUser().flatMapLatest { user ->
      if (user != null) {
        addressDao.getHistory(user.id).map { list ->
          list.map { it.toAddress() }
        }
      } else {
        flowOf(emptyList())
      }
    }
  }

  override suspend fun addToHistory(address: Address) {
    val user = userRepository.getUser().firstOrNull() ?: return
    val existing = addressDao.findHistoryEntryByLocation(
      userId = user.id,
      lat = address.latitude,
      lon = address.longitude
    )

    val entity = existing?.copy(lastUsedAt = getCurrentTimeMillis())
      ?: address.toEntity(user.id, type = null)

    addressDao.insertAddress(entity)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun getHomeAddress(): Flow<Address?> {
    return userRepository.getUser().flatMapLatest { user ->
      if (user != null) {
        addressDao.getAddressByType(user.id, "HOME").map { it?.toAddress() }
      } else {
        flowOf(null)
      }
    }
  }

  override suspend fun setHomeAddress(address: Address) {
    val user = userRepository.getUser().firstOrNull() ?: return

    addressDao.demoteHomeAddress(user.id)

    val existingEntry = addressDao.findHistoryEntryByLocation(
      userId = user.id,
      lat = address.latitude,
      lon = address.longitude
    )

    val entityToSave = existingEntry?.copy(
      type = "HOME",
      lastUsedAt = getCurrentTimeMillis(),
      formatted = address.formatted ?: existingEntry.formatted,
      street = address.street,
      houseNumber = address.houseNumber,
      zipCode = address.zipCode,
      city = address.city,
      country = address.country
    )
      ?: address.toEntity(user.id, type = "HOME")

    addressDao.insertAddress(entityToSave)
  }

  override suspend fun clearAllForUser() {
    val user = userRepository.getUser().firstOrNull() ?: return
    addressDao.deleteAllForUser(user.id)
  }
}