package com.example.community_app.ticket.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.data.local.favorite.FavoriteDao
import com.example.community_app.core.data.local.favorite.FavoriteEntity
import com.example.community_app.core.data.local.favorite.FavoriteType
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.location.LocationService
import com.example.community_app.core.domain.onError
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.dto.*
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.ticket.data.local.draft.TicketDraftDao
import com.example.community_app.ticket.data.local.ticket.TicketDao
import com.example.community_app.ticket.data.mappers.*
import com.example.community_app.ticket.data.network.RemoteTicketDataSource
import com.example.community_app.ticket.domain.Ticket
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.util.MediaTargetType
import com.example.community_app.util.SERVER_FETCH_INTERVAL_MS
import com.example.community_app.util.SERVER_FETCH_RADIUS_KM
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultTicketRepository(
  private val remoteTicketDataSource: RemoteTicketDataSource,
  private val mediaRepository: MediaRepository,
  private val ticketDao: TicketDao,
  private val ticketDraftDao: TicketDraftDao,
  private val favoriteDao: FavoriteDao,
  private val dataStore: DataStore<Preferences>,
  private val locationService: LocationService,
  private val authRepository: AuthRepository,
  private val fileStorage: FileStorage
): TicketRepository {
  private val keyLastSync = longPreferencesKey("ticket_last_sync_timestamp")

  override fun getTickets(): Flow<List<Ticket>> {
    return combine(
      ticketDao.getTickets(),
      favoriteDao.getFavoriteIds(FavoriteType.TICKET)
    ) { entities, favoriteIds ->
      val favSet = favoriteIds.toSet()
      entities.map { it.toTicket().copy(isFavorite = it.id in favSet) }
    }
  }

  override fun getTicket(id: Int): Flow<Ticket?> {
    return combine(
      ticketDao.getTicketById(id),
      favoriteDao.getFavoriteIds(FavoriteType.TICKET)
    ) { entity, favIds ->
      val isFav = id in favIds
      entity?.toTicket()?.copy(isFavorite = isFav)
    }
  }

  override fun getCommunityTickets(userId: Int): Flow<List<Ticket>> {
    return combine(
      ticketDao.getCommunityTickets(userId),
      favoriteDao.getFavoriteIds(FavoriteType.TICKET)
    ) { entities, favoriteIds ->
      val favSet = favoriteIds.toSet()
      entities.map { it.toTicket().copy(isFavorite = it.id in favSet) }
    }
  }

  override fun getUserTickets(userId: Int): Flow<List<Ticket>> {
    return ticketDao.getUserTickets(userId).map { list ->
      list.map { it.toTicket() }
    }
  }

  override suspend fun syncTickets(): Result<Unit, DataError.Remote> {
    val prefs = dataStore.data.first()
    val lastSync = prefs[keyLastSync] ?: 0L
    val now = getCurrentTimeMillis()

    if (now - lastSync < SERVER_FETCH_INTERVAL_MS) {
      return Result.Success(Unit)
    }

    return refreshTickets()
  }

  override suspend fun refreshTickets(): Result<Unit, DataError.Remote> = coroutineScope {
    val currentLocation = locationService.getCurrentLocation()

    val bboxString = if (currentLocation != null) {
      val bbox = GeoUtil.calculateBBox(currentLocation, SERVER_FETCH_RADIUS_KM)
      GeoUtil.toBBoxString(bbox)
    } else null

    val allTickets = mutableListOf<TicketDto>()

    val communityResult = remoteTicketDataSource.getTickets(bboxString)
    if (communityResult is Result.Success) {
      allTickets.addAll(communityResult.data)
    }

    val token = authRepository.getAccessToken()
    if (token != null) {
      val myResult = remoteTicketDataSource.getUserTickets()
      if (myResult is Result.Success) {
        allTickets.addAll(myResult.data)
      }
    }

    if (communityResult is Result.Error && token == null) {
      return@coroutineScope Result.Error(communityResult.error)
    }

    val loadedIds = allTickets.map { it.id }.toSet()
    val favoriteIds = favoriteDao.getFavoriteIds(FavoriteType.TICKET).first()
    val missingFavs = favoriteIds.filter { it !in loadedIds }

    val favDeferreds = missingFavs.map { id ->
      async { remoteTicketDataSource.getTicket(id) }
    }
    val favResults = favDeferreds.awaitAll()

    val additionalTickets = favResults.mapNotNull { if (it is Result.Success) it.data else null }
    allTickets.addAll(additionalTickets)

    val distinctTickets = allTickets.distinctBy { it.id }

    ticketDao.replaceAll(distinctTickets.map { it.toEntity() })
    dataStore.edit { it[keyLastSync] = getCurrentTimeMillis() }

    Result.Success(Unit)
  }

  override suspend fun refreshTicket(id: Int): Result<Unit, DataError.Remote> {
    return when (val result = remoteTicketDataSource.getTicket(id)) {
      is Result.Success -> {
        ticketDao.upsertTickets(listOf(result.data.toEntity()))
        Result.Success(Unit)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun getStatusHistory(id: Int): Result<List<TicketStatusDto>, DataError.Remote> {
    return remoteTicketDataSource.getStatusHistory(id)
  }

  override fun getDrafts(): Flow<List<TicketDraft>> {
    return ticketDraftDao.getDrafts().map { list ->
      list.map {
        val draft = it.toTicketDraft()
        draft.copy(images = draft.images.map { name ->
          fileStorage.getFullPath(name)
        })
      }
    }
  }

  override suspend fun getDraft(id: Long): TicketDraft? {
    val draft = ticketDraftDao.getDraftById(id)?.toTicketDraft() ?: return null
    return draft.copy(images = draft.images.map { name ->
      fileStorage.getFullPath(name)
    })
  }

  override suspend fun saveDraft(draft: TicketDraft): Long {
    return ticketDraftDao.upsertDraftFull(draft.toEntity(), draft.images)
  }

  override suspend fun deleteDraft(id: Long) {
    ticketDraftDao.deleteDraft(id)
  }

  override suspend fun uploadDraft(draft: TicketDraft): Result<Ticket, DataError.Remote> {
    val createRequest = TicketCreateDto(
      title = draft.title,
      description = draft.description,
      category = draft.category ?: TicketCategory.OTHER,
      officeId = draft.officeId ?: 1, // Mock
      address = draft.address?.let {
        AddressDto(it.street, it.houseNumber, it.zipCode, it.city, it.longitude, it.latitude)
      },
      visibility = draft.visibility
    )

    val ticketResult = remoteTicketDataSource.createTicket(createRequest)

    if (ticketResult is Result.Error) {
      return Result.Error(ticketResult.error)
    }

    val newTicket = (ticketResult as Result.Success).data
    val ticketId = newTicket.id

    draft.images.forEach { imageString ->
      val fileName = if (imageString.contains("/")) {
        imageString.substringAfterLast("/")
      } else {
        imageString
      }

      mediaRepository.uploadMedia(
        targetType = MediaTargetType.TICKET,
        targetId = ticketId,
        fileName = fileName
      )
    }

    val refreshResult = refreshTicket(ticketId)
    ticketDraftDao.deleteDraft(draft.id)

    return if(refreshResult is Result.Success) {
      val entity = ticketDao.getTicketById(ticketId).first()

      if (entity != null) {
        Result.Success(entity.toTicket())
      } else {
        Result.Error(DataError.Remote.UNKNOWN)
      }
    } else {
      Result.Success(newTicket.toEntity().toTicket())
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

        val updatedTicket = ticketDao.getTicketById(id).first()?.toTicket()
          ?: result.data.toEntity().toTicket()

        val isFav = favoriteDao.isFavorite(id, FavoriteType.TICKET)
        Result.Success(updatedTicket.copy(isFavorite = isFav))
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun deleteTicket(id: Int): Result<Unit, DataError.Remote> {
    return when(val result = remoteTicketDataSource.deleteTicket(id)) {
      is Result.Success -> {
        toggleFavorite(id, false)
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

  override suspend fun toggleFavorite(ticketId: Int, isFavorite: Boolean) {
    val entity = FavoriteEntity(itemId = ticketId, type = FavoriteType.TICKET)
    if (isFavorite) {
      favoriteDao.addFavorite(entity)
    } else {
      favoriteDao.removeFavorite(entity)
    }
  }
}