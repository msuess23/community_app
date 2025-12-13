package com.example.community_app.ticket.data.network

import com.example.community_app.core.data.safeCall
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.TicketCreateDto
import com.example.community_app.dto.TicketDto
import com.example.community_app.dto.TicketStatusDto
import com.example.community_app.dto.TicketUpdateDto
import com.example.community_app.dto.TicketVoteSummaryDto
import com.example.community_app.util.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorRemoteTicketDataSource(
  private val httpClient: HttpClient
): RemoteTicketDataSource {
  override suspend fun getTickets(bbox: String?): Result<List<TicketDto>, DataError.Remote> {
    return safeCall {
      httpClient.get(urlString = "$BASE_URL/api/ticket") {
        if (bbox != null) parameter("bbox", bbox)
      }
    }
  }

  override suspend fun getUserTickets(): Result<List<TicketDto>, DataError.Remote> {
    return safeCall {
      httpClient.get(urlString = "$BASE_URL/api/ticket/mine")
    }
  }

  override suspend fun getTicket(id: Int): Result<TicketDto, DataError.Remote> {
    return safeCall {
      httpClient.get(urlString = "$BASE_URL/api/ticket/$id")
    }
  }

  override suspend fun getStatusHistory(id: Int): Result<List<TicketStatusDto>, DataError.Remote> {
    return safeCall { httpClient.get("$BASE_URL/api/ticket/$id/status")}
  }

  override suspend fun getCurrentStatus(id: Int): Result<TicketStatusDto?, DataError.Remote> {
    return safeCall {
      httpClient.get("$BASE_URL/api/ticket/$id/status/current")
    }
  }

  override suspend fun createTicket(request: TicketCreateDto): Result<TicketDto, DataError.Remote> {
    return safeCall {
      httpClient.post("$BASE_URL/api/ticket") {
        contentType(ContentType.Application.Json)
        setBody(request)
      }
    }
  }

  override suspend fun updateTicket(id: Int, request: TicketUpdateDto): Result<TicketDto, DataError.Remote> {
    return safeCall {
      httpClient.put("$BASE_URL/api/ticket/$id") {
        contentType(ContentType.Application.Json)
        setBody(request)
      }
    }
  }

  override suspend fun deleteTicket(id: Int): Result<Unit, DataError.Remote> {
    return safeCall {
      httpClient.delete("$BASE_URL/api/ticket/$id")
    }
  }

  override suspend fun voteTicket(id: Int): Result<TicketVoteSummaryDto, DataError.Remote> {
    return safeCall {
      httpClient.post("$BASE_URL/api/ticket/$id/vote")
    }
  }

  override suspend fun unvoteTicket(id: Int): Result<TicketVoteSummaryDto, DataError.Remote> {
    return safeCall {
      httpClient.delete("$BASE_URL/api/ticket/$id/vote")
    }
  }
}