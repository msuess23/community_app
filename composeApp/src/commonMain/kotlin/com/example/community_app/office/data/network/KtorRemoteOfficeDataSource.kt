package com.example.community_app.office.data.network

import com.example.community_app.core.data.safeCall
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.OfficeDto
import com.example.community_app.util.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorRemoteOfficeDataSource(
  private val httpClient: HttpClient
) : RemoteOfficeDataSource {

  override suspend fun getOffices(bbox: String?): Result<List<OfficeDto>, DataError.Remote> {
    return safeCall {
      httpClient.get("$BASE_URL/api/office") {
        if (bbox != null) {
          parameter("bbox", bbox)
        }
      }
    }
  }

  override suspend fun getOffice(id: Int): Result<OfficeDto, DataError.Remote> {
    return safeCall {
      httpClient.get("$BASE_URL/api/office/$id")
    }
  }
}