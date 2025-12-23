package com.example.community_app.ticket.data.repository

import com.example.community_app.auth.domain.repository.AuthRepository
import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.data.local.favorite.FavoriteDao
import com.example.community_app.core.data.local.favorite.FavoriteEntity
import com.example.community_app.core.data.local.favorite.FavoriteType
import com.example.community_app.core.data.sync.SyncManager
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.*
import com.example.community_app.geocoding.data.mappers.toDto
import com.example.community_app.geocoding.domain.model.Address
import com.example.community_app.media.domain.repository.MediaRepository
import com.example.community_app.profile.domain.repository.UserRepository
import com.example.community_app.ticket.data.local.draft.TicketDraftDao
import com.example.community_app.ticket.data.local.ticket.TicketDao
import com.example.community_app.ticket.data.mappers.*
import com.example.community_app.ticket.data.network.RemoteTicketDataSource
import com.example.community_app.ticket.domain.model.Ticket
import com.example.community_app.ticket.domain.model.TicketDraft
import com.example.community_app.ticket.domain.repository.TicketRepository
import com.example.community_app.ticket.domain.model.TicketStatusEntry
import com.example.community_app.util.MediaTargetType
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class DefaultTicketRepository(
  private val remoteTicketDataSource: RemoteTicketDataSource,
  private val mediaRepository: MediaRepository,
  private val ticketDao: TicketDao,
  private val ticketDraftDao: TicketDraftDao,
  private val favoriteDao: FavoriteDao,
  private val syncManager: SyncManager,
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository,
  private val fileStorage: FileStorage
): TicketRepository {
  override fun getTickets(): Flow<List<Ticket>> {
    return combine(
      ticketDao.getTickets(),
      getFavoritesFlow()
    ) { entities, favoriteIds ->
      val favSet = favoriteIds.toSet()
      entities.map { it.toTicket().copy(isFavorite = it.id in favSet) }
    }
  }

  override fun getTicket(id: Int): Flow<Ticket?> {
    return combine(
      ticketDao.getTicketById(id),
      getFavoritesFlow()
    ) { entity, favIds ->
      val isFav = id in favIds
      entity?.toTicket()?.copy(isFavorite = isFav)
    }
  }

  override fun getCommunityTickets(userId: Int): Flow<List<Ticket>> {
    return combine(
      ticketDao.getCommunityTickets(userId),
      getFavoritesFlow()
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

  override suspend fun refreshTickets(force: Boolean): Result<Unit, DataError.Remote> = coroutineScope {
    val decision = syncManager.checkSyncStatus(
      featureKey = "ticket",
      forceRefresh = force
    )

    if (!decision.shouldFetch) {
      return@coroutineScope Result.Success(Unit)
    }

    val communityDeferred = async { remoteTicketDataSource.getTickets(decision.bboxString) }

    val token = authRepository.getAccessToken()
    val userDeferred = if (token != null) {
      async { remoteTicketDataSource.getUserTickets() }
    } else null

    val communityResult = communityDeferred.await()
    val userResult = userDeferred?.await()

    val allTickets = mutableListOf<TicketDto>()

    when (communityResult) {
      is Result.Error -> return@coroutineScope Result.Error(communityResult.error)
      is Result.Success -> allTickets.addAll(communityResult.data)
    }

    when (userResult) {
      is Result.Error -> return@coroutineScope Result.Error(userResult.error)
      is Result.Success -> allTickets.addAll(userResult.data)
      else -> {}
    }

    val user = userRepository.getUser().firstOrNull()
    if (user != null) {
      val loadedIds = allTickets.map { it.id }.toSet()
      val favoriteIds = favoriteDao.getFavoriteIds(user.id, FavoriteType.TICKET).first()
      val missingFavs = favoriteIds.filter { it !in loadedIds }

      if (missingFavs.isNotEmpty()) {
        val favResults = missingFavs.map { id ->
          async { remoteTicketDataSource.getTicket(id) }
        }.awaitAll()

        val fetchedFavorites = favResults.mapNotNull { if (it is Result.Success) it.data else null }
        allTickets.addAll(fetchedFavorites)
      }
    }

    val distinctTickets = allTickets.distinctBy { it.id }

    ticketDao.replaceAll(distinctTickets.map { it.toEntity() })

    syncManager.updateSyncSuccess("ticket", decision.currentLocation)

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

  override suspend fun getStatusHistory(id: Int): Result<List<TicketStatusEntry>, DataError.Remote> {
    return when (val result = remoteTicketDataSource.getStatusHistory(id)) {
      is Result.Success -> {
        val domainList = result.data.map { it.toDomain() }
        Result.Success(domainList)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun getCurrentStatus(id: Int): Result<TicketStatusEntry?, DataError.Remote> {
    return when (val result = remoteTicketDataSource.getCurrentStatus(id)) {
      is Result.Success -> {
        Result.Success(result.data?.toDomain())
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun getDrafts(): Flow<List<TicketDraft>> {
    return userRepository.getUser().flatMapLatest { user ->
      if (user != null) {
        ticketDraftDao.getDrafts(user.id).map { list ->
          list.map {
            val draft = it.toTicketDraft()
            draft.copy(images = draft.images.map { name ->
              fileStorage.getFullPath(name)
            })
          }
        }
      } else {
        flowOf(emptyList())
      }
    }
  }


  override suspend fun getDraft(id: Long): TicketDraft? {
    val user = userRepository.getUser().firstOrNull() ?: return null
    val draftRelation = ticketDraftDao.getDraftById(id, user.id) ?: return null

    val draft = draftRelation.toTicketDraft()
    return draft.copy(images = draft.images.map { name ->
      fileStorage.getFullPath(name)
    })
  }

  override suspend fun saveDraft(draft: TicketDraft): Long {
    val user = userRepository.getUser().firstOrNull() ?: throw IllegalStateException("No user")
    return ticketDraftDao.upsertDraftFull(draft.toEntity(user.id), draft.images)
  }

  override suspend fun deleteDraft(id: Long) {
    ticketDraftDao.deleteDraft(id)
  }

  override suspend fun uploadDraft(draft: TicketDraft): Result<Ticket, DataError.Remote> {
    val createRequest = TicketCreateDto(
      title = draft.title,
      description = draft.description,
      category = draft.category ?: TicketCategory.OTHER,
      officeId = draft.officeId ?: 1,
      address = draft.address?.toDto(),
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
    address: Address?,
    visibility: TicketVisibility?
  ): Result<Ticket, DataError.Remote> {
    val request = TicketUpdateDto(title, description, category, officeId, address?.toDto(), visibility)

    return when(val result = remoteTicketDataSource.updateTicket(id, request)) {
      is Result.Success -> {
        ticketDao.upsertTickets(listOf(result.data.toEntity()))

        val updatedTicket = ticketDao.getTicketById(id).first()?.toTicket()
          ?: result.data.toEntity().toTicket()

        val user = userRepository.getUser().firstOrNull()
        val isFav = if (user != null) {
          favoriteDao.isFavorite(user.id, id, FavoriteType.TICKET)
        } else false
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
    val user = userRepository.getUser().firstOrNull() ?: return

    val entity = FavoriteEntity(
      userId = user.id,
      itemId = ticketId,
      type = FavoriteType.TICKET
    )

    if (isFavorite) {
      favoriteDao.addFavorite(entity)
    } else {
      favoriteDao.removeFavorite(entity)
    }
  }

  override suspend fun clearUserData() {
    val user = userRepository.getUser().firstOrNull() ?: return
    favoriteDao.clearFavoritesForUser(user.id)
    ticketDraftDao.deleteDraftsForUser(user.id)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun getFavoritesFlow(): Flow<Set<Int>> {
    return userRepository.getUser()
      .distinctUntilChanged()
      .flatMapLatest { user ->
        if (user != null) {
          favoriteDao.getFavoriteIds(user.id, FavoriteType.TICKET).map { it.toSet() }
        } else {
          flowOf(emptySet())
        }
      }
  }
}