package com.example.community_app.geocoding.data.network

import com.example.community_app.BuildKonfig
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorRemoteGeocodingDataSource(
  private val httpClient: HttpClient
) : RemoteGeocodingDataSource {
  private val apiKey = BuildKonfig.GEOAPIFY_KEY
  private val baseUrl = BuildKonfig.GEOAPIFY_URL

  override suspend fun search(query: String): Result<GeoapifyResponseDto, DataError.Remote> {
    return try {
      val response = httpClient.get("$baseUrl/search") {
        parameter("text", query)
        parameter("apiKey", apiKey)
        parameter("lang", "de")
        parameter("limit", 5)
      }
      Result.Success(response.body())
    } catch (e: Exception) {
      e.printStackTrace()
      Result.Error(DataError.Remote.UNKNOWN)
    }
  }

  override suspend fun reverse(lat: Double, lon: Double): Result<GeoapifyResponseDto, DataError.Remote> {
    return try {
      val response = httpClient.get("$baseUrl/reverse") {
        parameter("lat", lat)
        parameter("lon", lon)
        parameter("apiKey", apiKey)
        parameter("lang", "de")
      }
      Result.Success(response.body())
    } catch (e: Exception) {
      e.printStackTrace()
      Result.Error(DataError.Remote.UNKNOWN)
    }
  }
}