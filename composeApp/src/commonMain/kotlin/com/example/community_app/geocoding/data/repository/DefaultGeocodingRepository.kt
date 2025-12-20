package com.example.community_app.geocoding.data.repository

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.geocoding.data.network.GeoapifyPropertiesDto
import com.example.community_app.geocoding.data.network.RemoteGeocodingDataSource
import com.example.community_app.geocoding.domain.Address
import com.example.community_app.geocoding.domain.GeocodingRepository

class DefaultGeocodingRepository(
  private val remoteDataSource: RemoteGeocodingDataSource
) : GeocodingRepository {

  override suspend fun searchAddress(query: String): Result<List<Address>, DataError.Remote> {
    return when (val result = remoteDataSource.search(query)) {
      is Result.Success -> {
        val addresses = result.data.features.map { it.properties.toDomain() }
        Result.Success(addresses)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun getAddressFromCoordinates(lat: Double, lon: Double): Result<Address?, DataError.Remote> {
    return when (val result = remoteDataSource.reverse(lat, lon)) {
      is Result.Success -> {
        val address = result.data.features.firstOrNull()?.properties?.toDomain()
        Result.Success(address)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  private fun GeoapifyPropertiesDto.toDomain(): Address {
    val fallbackFormatted = listOfNotNull(street, housenumber, postcode, city).joinToString(" ")

    return Address(
      formatted = formatted ?: fallbackFormatted,
      street = street,
      houseNumber = housenumber,
      zipCode = postcode,
      city = city,
      country = country,
      latitude = lat,
      longitude = lon
    )
  }
}