package com.example.community_app.info.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.dto.InfoStatusDto
import com.example.community_app.info.data.local.InfoDao
import com.example.community_app.info.data.mappers.toEntity
import com.example.community_app.info.data.mappers.toInfo
import com.example.community_app.info.data.network.RemoteInfoDataSource
import com.example.community_app.info.domain.Info
import com.example.community_app.info.domain.InfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultInfoRepository(
  private val remoteInfoDataSource: RemoteInfoDataSource,
  private val infoDao: InfoDao,
  private val dataStore: DataStore<Preferences>
): InfoRepository {
  private val KEY_LAST_SYNC = longPreferencesKey("info_last_sync_timestamp")
  private val CACHE_TIMEOUT = 24 * 60 * 60 * 1000L

  override suspend fun syncInfos(): Result<Unit, DataError.Remote> {
    val prefs = dataStore.data.first()
    val lastSync = prefs[KEY_LAST_SYNC] ?: 0L
    val now = getCurrentTimeMillis()

    if (now - lastSync < CACHE_TIMEOUT) {
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
    return when (val result = remoteInfoDataSource.getInfos()) {
      is Result.Success -> {
        try {
          val entities = result.data.map { it.toEntity() }
          infoDao.replaceAll(entities)

          dataStore.edit { it[KEY_LAST_SYNC] = getCurrentTimeMillis() }

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