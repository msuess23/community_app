package com.example.community_app.geocoding.data.network

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result

interface RemoteGeocodingDataSource {
  suspend fun search(query: String): Result<GeoapifyResponseDto, DataError.Remote>
  suspend fun reverse(lat: Double, lon: Double): Result<GeoapifyResponseDto, DataError.Remote>
}