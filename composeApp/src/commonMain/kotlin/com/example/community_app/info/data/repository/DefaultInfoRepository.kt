package com.example.community_app.info.data.repository

import com.example.community_app.core.data.local.favorite.FavoriteDao
import com.example.community_app.core.data.local.favorite.FavoriteEntity
import com.example.community_app.core.data.local.favorite.FavoriteType
import com.example.community_app.core.data.sync.SyncManager
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.InfoDto
import com.example.community_app.dto.InfoStatusDto
import com.example.community_app.info.data.local.InfoDao
import com.example.community_app.info.data.mappers.toEntity
import com.example.community_app.info.data.mappers.toInfo
import com.example.community_app.info.data.network.RemoteInfoDataSource
import com.example.community_app.info.domain.Info
import com.example.community_app.info.domain.InfoRepository
import com.example.community_app.profile.domain.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
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
import kotlinx.coroutines.withTimeout
import kotlin.collections.map

class DefaultInfoRepository(
  private val remoteInfoDataSource: RemoteInfoDataSource,
  private val infoDao: InfoDao,
  private val favoriteDao: FavoriteDao,
  private val syncManager: SyncManager,
  private val userRepository: UserRepository,
): InfoRepository {
  override fun getInfos(): Flow<List<Info>> {
    return combine(
      infoDao.getInfos(),
      getFavoritesFlow()
    ) { entities, favoriteIds ->
      val favSet = favoriteIds.toSet()
      entities.map { entity ->
        entity.toInfo().copy(isFavorite = entity.id in favSet)
      }
    }
  }

  override fun getInfo(id: Int): Flow<Info?> {
    return combine(
      infoDao.getInfoById(id),
      getFavoritesFlow()
    ) { entity, favIds ->
      entity?.toInfo()?.copy(isFavorite = entity.id in favIds)
    }
  }

  override suspend fun refreshInfos(force: Boolean): Result<Unit, DataError.Remote> = coroutineScope {
    try {
      withTimeout(10_000L) {
        val decision = syncManager.checkSyncStatus(
          featureKey = "info",
          forceRefresh = force
        )

        if (!decision.shouldFetch) {
          return@withTimeout Result.Success(Unit)
        }

        val bboxResult = remoteInfoDataSource.getInfos(decision.bboxString)

        val allInfos = mutableListOf<InfoDto>()

        when (bboxResult) {
          is Result.Error -> return@withTimeout Result.Error(bboxResult.error)
          is Result.Success -> allInfos.addAll(bboxResult.data)
        }

        val user = userRepository.getUser().firstOrNull()
        if (user != null) {
          val loadedIds = allInfos.map { it.id }.toSet()
          val favoriteIds = favoriteDao.getFavoriteIds(user.id, FavoriteType.INFO).first()
          val missingFavs = favoriteIds.filter { it !in loadedIds }

          if (missingFavs.isNotEmpty()) {
            val favResults = missingFavs.map { id ->
              async { remoteInfoDataSource.getInfo(id) }
            }.awaitAll()

            val fetchedFavorites = favResults.mapNotNull { if (it is Result.Success) it.data else null }
            allInfos.addAll(fetchedFavorites)
          }
        }

        val distinctInfos = allInfos.distinctBy { it.id }

        try {
          val entities = distinctInfos.map { it.toEntity() }
          infoDao.replaceAll(entities)

          syncManager.updateSyncSuccess("info", decision.currentLocation)

          Result.Success(Unit)
        } catch (e: Exception) {
          e.printStackTrace()
          Result.Error(DataError.Remote.UNKNOWN)
        }
      }
    } catch (e: TimeoutCancellationException) {
      Result.Error(DataError.Remote.REQUEST_TIMEOUT)
    } catch (e: Exception) {
      e.printStackTrace()
      Result.Error(DataError.Remote.UNKNOWN)
    }
  }

  override suspend fun refreshInfo(id: Int): Result<Unit, DataError.Remote> {
    return when (val result = remoteInfoDataSource.getInfo(id)) {
      is Result.Success -> {
        try {
          val entity = result.data.toEntity()
          infoDao.upsertInfos(listOf(entity))
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

  override suspend fun getStatusHistory(id: Int): Result<List<InfoStatusDto>, DataError.Remote> {
    return remoteInfoDataSource.getStatusHistory(id)
  }

  override suspend fun getCurrentStatus(id: Int): Result<InfoStatusDto?, DataError.Remote> {
    return remoteInfoDataSource.getCurrentStatus(id)
  }

  override suspend fun toggleFavorite(infoId: Int, isFavorite: Boolean) {
    val user = userRepository.getUser().firstOrNull() ?: return

    val entity = FavoriteEntity(
      userId = user.id,
      itemId = infoId,
      type = FavoriteType.INFO
    )

    if (isFavorite) {
      favoriteDao.addFavorite(entity)
    } else {
      favoriteDao.removeFavorite(entity)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun getFavoritesFlow(): Flow<Set<Int>> {
    return userRepository.getUser()
      .distinctUntilChanged()
      .flatMapLatest { user ->
        if (user != null) {
          favoriteDao.getFavoriteIds(user.id, FavoriteType.INFO).map { it.toSet() }
        } else {
          flowOf(emptySet())
        }
      }
  }
}