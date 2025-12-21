package com.example.community_app.ticket.domain

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.geocoding.domain.Address
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
  fun getTickets(): Flow<List<Ticket>>
  fun getTicket(id: Int): Flow<Ticket?>
  fun getCommunityTickets(userId: Int): Flow<List<Ticket>>
  fun getUserTickets(userId: Int): Flow<List<Ticket>>

  suspend fun refreshTickets(force: Boolean = true): Result<Unit, DataError.Remote>
  suspend fun refreshTicket(id: Int): Result<Unit, DataError.Remote>
  suspend fun getStatusHistory(id: Int): Result<List<TicketStatusEntry>, DataError.Remote>
  suspend fun getCurrentStatus(id: Int): Result<TicketStatusEntry?, DataError.Remote>

  fun getDrafts(): Flow<List<TicketDraft>>
  suspend fun getDraft(id: Long): TicketDraft?
  suspend fun saveDraft(draft: TicketDraft): Long
  suspend fun deleteDraft(id: Long)

  suspend fun uploadDraft(draft: TicketDraft): Result<Ticket, DataError.Remote>

  suspend fun updateTicket(
    id: Int,
    title: String?,
    description: String?,
    category: TicketCategory?,
    officeId: Int?,
    address: Address?,
    visibility: TicketVisibility?
  ): Result<Ticket, DataError.Remote>

  suspend fun deleteTicket(id: Int): Result<Unit, DataError.Remote>

  suspend fun voteTicket(id: Int): Result<Unit, DataError.Remote>
  suspend fun unvoteTicket(id: Int): Result<Unit, DataError.Remote>

  suspend fun toggleFavorite(ticketId: Int, isFavorite: Boolean)

  suspend fun clearUserData()
}