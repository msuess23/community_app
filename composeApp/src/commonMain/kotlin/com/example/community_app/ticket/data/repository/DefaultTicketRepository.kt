package com.example.community_app.ticket.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.location.LocationService
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.dto.AddressDto
import com.example.community_app.dto.TicketCreateDto
import com.example.community_app.dto.TicketStatusDto
import com.example.community_app.dto.TicketUpdateDto
import com.example.community_app.ticket.data.local.draft.TicketDraftDao
import com.example.community_app.ticket.data.local.ticket.TicketDao
import com.example.community_app.ticket.data.mappers.toEntity
import com.example.community_app.ticket.data.mappers.toTicket
import com.example.community_app.ticket.data.mappers.toTicketDraft
import com.example.community_app.ticket.data.network.RemoteTicketDataSource
import com.example.community_app.ticket.domain.Ticket
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.util.SERVER_FETCH_INTERVAL_MS
import com.example.community_app.util.SERVER_FETCH_RADIUS_KM
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultTicketRepository(
  private val remoteTicketDataSource: RemoteTicketDataSource,
  private val ticketDao: TicketDao,
  private val ticketDraftDao: TicketDraftDao,
  private val dataStore: DataStore<Preferences>,
  private val locationService: LocationService
): TicketRepository {
  private val keyLastSync = longPreferencesKey("ticket_last_sync_timestamp")

  override suspend fun syncTickets(): Result<Unit, DataError.Remote> {
    val prefs = dataStore.data.first()
    val lastSync = prefs[keyLastSync] ?: 0L
    val now = getCurrentTimeMillis()

    if (now - lastSync < SERVER_FETCH_INTERVAL_MS) {
      return Result.Success(Unit)
    }

    return refreshTickets()
  }

  override fun getTickets(): Flow<List<Ticket>> {
    return ticketDao.getTickets().map { entities ->
      entities.map { it.toTicket() }
    }
  }

  override fun getTicket(id: Int): Flow<Ticket?> {
    return ticketDao.getTicketById(id).map { entity ->
      entity?.toTicket()
    }
  }

  override suspend fun refreshTickets(): Result<Unit, DataError.Remote> {
    val currentLocation = locationService.getCurrentLocation()

    val bboxString = if (currentLocation != null) {
      println("DefaultTicketRepository: Location found: $currentLocation")
      val bbox = GeoUtil.calculateBBox(currentLocation, SERVER_FETCH_RADIUS_KM)
      GeoUtil.toBBoxString(bbox)
    } else {
      println("DefaultTicketRepository: WARNING - No location available for BBox filter!")
      null
    }

    return when (val result = remoteTicketDataSource.getTickets(bboxString)) {
      is Result.Success -> {
        try {
          val entities = result.data.map { it.toEntity() }
          ticketDao.replaceAll(entities)

          dataStore.edit { it[keyLastSync] = getCurrentTimeMillis() }

          Result.Success(Unit)
        } catch (e: Exception) {
          e.printStackTrace()
          Result.Error(DataError.Remote.UNKNOWN)
        }
      }
      is Result.Error -> {
        Result.Error(result.error)
      }
    }
  }

  override suspend fun refreshTicket(id: Int): Result<Unit, DataError.Remote> {
    return when (val result = remoteTicketDataSource.getTicket(id)) {
      is Result.Success -> {
        try {
          val entity = result.data.toEntity()
          ticketDao.upsertTickets(listOf(entity))
          Result.Success(Unit)
        } catch (e: Exception) {
          e.printStackTrace()
          Result.Error(DataError.Remote.UNKNOWN)
        }
      }
      is Result.Error -> {
        Result.Error(result.error)
      }
    }
  }

  override suspend fun getStatusHistory(id: Int): Result<List<TicketStatusDto>, DataError.Remote> {
    return remoteTicketDataSource.getStatusHistory(id)
  }

  override suspend fun createTicket(
    title: String,
    description: String,
    category: TicketCategory,
    officeId: Int,
    address: AddressDto,
    visibility: TicketVisibility
  ): Result<Ticket, DataError.Remote> {
    val request = TicketCreateDto(title, description, category, officeId, address, visibility)
    return when(val result = remoteTicketDataSource.createTicket(request)) {
      is Result.Success -> {
        ticketDao.upsertTickets(listOf(result.data.toEntity()))
        Result.Success(result.data.toEntity().toTicket())
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun updateTicket(
    id: Int,
    title: String?,
    description: String?,
    category: TicketCategory?,
    officeId: Int?,
    address: AddressDto?,
    visibility: TicketVisibility?
  ): Result<Ticket, DataError.Remote> {
    val request = TicketUpdateDto(title, description, category, officeId, address, visibility)
    return when(val result = remoteTicketDataSource.updateTicket(id, request)) {
      is Result.Success -> {
        ticketDao.upsertTickets(listOf(result.data.toEntity()))
        Result.Success(result.data.toEntity().toTicket())
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun deleteTicket(id: Int): Result<Unit, DataError.Remote> {
    return when(val result = remoteTicketDataSource.deleteTicket(id)) {
      is Result.Success -> {
        refreshTickets()
        Result.Success(Unit)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun voteTicket(id: Int): Result<Unit, DataError.Remote> {
    return when(val result = remoteTicketDataSource.voteTicket(id)) {
      is Result.Success -> {
        refreshTicket(id)
        Result.Success(Unit)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun unvoteTicket(id: Int): Result<Unit, DataError.Remote> {
    return when(val result = remoteTicketDataSource.unvoteTicket(id)) {
      is Result.Success -> {
        refreshTicket(id)
        Result.Success(Unit)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override fun getDrafts(): Flow<List<TicketDraft>> {
    return ticketDraftDao.getDrafts().map { list->
      list.map { it.toTicketDraft() }
    }
  }

  override suspend fun getDraft(id: Long): TicketDraft? {
    return ticketDraftDao.getDraftById(id)?.toTicketDraft()
  }

  override suspend fun saveDraft(draft: TicketDraft): Long {
    return ticketDraftDao.upsertDraft(draft.toEntity())
  }

  override suspend fun deleteDraft(id: Long) {
    ticketDraftDao.deleteDraft(id)
  }
}