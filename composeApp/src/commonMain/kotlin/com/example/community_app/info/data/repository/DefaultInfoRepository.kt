package com.example.community_app.info.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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
import com.example.community_app.util.SERVER_FETCH_RADIUS_KM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultInfoRepository(
  private val remoteInfoDataSource: RemoteInfoDataSource,
  private val infoDao: InfoDao,
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
    return infoDao.getInfos().map { entities ->
      entities.map { it.toInfo() }
    }
  }

  override fun getInfo(id: Int): Flow<Info?> {
    return infoDao.getInfoById(id).map { entity ->
      entity?.toInfo()
    }
  }

  override suspend fun refreshInfos(): Result<Unit, DataError.Remote> {
    val currentLocation = locationService.getCurrentLocation()

    val bboxString = if (currentLocation != null) {
      println("DefaultInfoRepository: Location found: $currentLocation")
      val bbox = GeoUtil.calculateBBox(currentLocation, SERVER_FETCH_RADIUS_KM)
      GeoUtil.toBBoxString(bbox)
    } else {
      println("DefaultInfoRepository: WARNING - No location available for BBox filter!")
      null
    }

    return when (val result = remoteInfoDataSource.getInfos(bboxString)) {
      is Result.Success -> {
        try {
          val entities = result.data.map { it.toEntity() }
          infoDao.replaceAll(entities)

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
}