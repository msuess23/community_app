package com.example.community_app.info.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.community_app.core.data.local.favorite.FavoriteDao
import com.example.community_app.core.data.local.favorite.FavoriteEntity
import com.example.community_app.core.data.local.favorite.FavoriteType
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.location.LocationService
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.dto.InfoStatusDto
import com.example.community_app.info.data.local.InfoDao
import com.example.community_app.info.data.mappers.toEntity
import com.example.community_app.info.data.mappers.toInfo
import com.example.community_app.info.data.network.RemoteInfoDataSource
import com.example.community_app.info.domain.Info
import com.example.community_app.info.domain.InfoRepository
import com.example.community_app.util.SERVER_FETCH_INTERVAL_MS
import com.example.community_app.util.SERVER_FILTER_RADIUS_KM
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class DefaultInfoRepository(
  private val remoteInfoDataSource: RemoteInfoDataSource,
  private val infoDao: InfoDao,
  private val favoriteDao: FavoriteDao,
  private val dataStore: DataStore<Preferences>,
  private val locationService: LocationService
): InfoRepository {
  private val keyLastSync = longPreferencesKey("info_last_sync_timestamp")

  override suspend fun syncInfos(): Result<Unit, DataError.Remote> {
    val prefs = dataStore.data.first()
    val lastSync = prefs[keyLastSync] ?: 0L
    val now = getCurrentTimeMillis()

    if (now - lastSync < SERVER_FETCH_INTERVAL_MS) {
      return Result.Success(Unit)
    }

    return refreshInfos()
  }

  override fun getInfos(): Flow<List<Info>> {
    return combine(
      infoDao.getInfos(),
      favoriteDao.getFavoriteIds(FavoriteType.INFO)
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
      favoriteDao.getFavoriteIds(FavoriteType.INFO)
    ) { entity, favIds ->
      entity?.toInfo()?.copy(isFavorite = entity.id in favIds)
    }
  }

  override suspend fun refreshInfos(): Result<Unit, DataError.Remote> = coroutineScope {
    val currentLocation = locationService.getCurrentLocation()

    val bboxString = if (currentLocation != null) {
      println("DefaultInfoRepository: Location found: $currentLocation")
      val bbox = GeoUtil.calculateBBox(currentLocation, SERVER_FILTER_RADIUS_KM)
      GeoUtil.toBBoxString(bbox)
    } else {
      println("DefaultInfoRepository: WARNING - No location available for BBox filter!")
      null
    }

    val bboxResult = remoteInfoDataSource.getInfos(bboxString)
    if (bboxResult is Result.Error) {
      return@coroutineScope Result.Error(bboxResult.error)
    }

    val bboxInfos = (bboxResult as Result.Success).data

    val favoriteIds = favoriteDao.getFavoriteIds(FavoriteType.INFO).first()
    val loadedIds = bboxInfos.map { it.id }.toSet()
    val missingFavoriteIds = favoriteIds.filter { it !in loadedIds }

    val favoriteDeferreds = missingFavoriteIds.map { id ->
      async { remoteInfoDataSource.getInfo(id) }
    }
    val favoriteResults = favoriteDeferreds.awaitAll()

    val additionalFavorites = favoriteResults
      .mapNotNull { if (it is Result.Success) it.data else null }

    val allInfos = (bboxInfos + additionalFavorites).distinctBy { it.id }

    try {
      val entities = allInfos.map { it.toEntity() }
      infoDao.replaceAll(entities)

      dataStore.edit { it[keyLastSync] = getCurrentTimeMillis() }

      Result.Success(Unit)
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
    val entity = FavoriteEntity(itemId = infoId, type = FavoriteType.INFO)
    if (isFavorite) {
      favoriteDao.addFavorite(entity)
    } else {
      favoriteDao.removeFavorite(entity)
    }
  }
}