package com.example.community_app.ticket.data.network

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.TicketCreateDto
import com.example.community_app.dto.TicketDto
import com.example.community_app.dto.TicketStatusDto
import com.example.community_app.dto.TicketUpdateDto
import com.example.community_app.dto.TicketVoteSummaryDto

interface RemoteTicketDataSource {
  suspend fun getTickets(bbox: String? = null): Result<List<TicketDto>, DataError.Remote>
  suspend fun getTicket(id: Int): Result<TicketDto, DataError.Remote>
  suspend fun getStatusHistory(id: Int): Result<List<TicketStatusDto>, DataError.Remote>

  suspend fun createTicket(request: TicketCreateDto): Result<TicketDto, DataError.Remote>
  suspend fun updateTicket(id: Int, request: TicketUpdateDto): Result<TicketDto, DataError.Remote>
  suspend fun deleteTicket(id: Int): Result<Unit, DataError.Remote>

  suspend fun voteTicket(id: Int): Result<TicketVoteSummaryDto, DataError.Remote>
  suspend fun unvoteTicket(id: Int): Result<TicketVoteSummaryDto, DataError.Remote>
}