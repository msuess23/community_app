package com.example.community_app.office.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.location.LocationService
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.office.data.local.OfficeDao
import com.example.community_app.office.data.mappers.toEntity
import com.example.community_app.office.data.mappers.toOffice
import com.example.community_app.office.data.network.RemoteOfficeDataSource
import com.example.community_app.office.domain.Office
import com.example.community_app.office.domain.OfficeRepository
import com.example.community_app.util.SERVER_FETCH_INTERVAL_MS
import com.example.community_app.util.SERVER_FETCH_RADIUS_KM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultOfficeRepository(
  private val remoteOfficeDataSource: RemoteOfficeDataSource,
  private val officeDao: OfficeDao,
  private val dataStore: DataStore<Preferences>,
  private val locationService: LocationService
) : OfficeRepository {

  private val keyLastSync = longPreferencesKey("office_last_sync_timestamp")

  override fun getOffices(): Flow<List<Office>> {
    return officeDao.getOffices().map { entities ->
      entities.map { it.toOffice() }
    }
  }

  override fun getOffice(id: Int): Flow<Office?> {
    return officeDao.getOfficeById(id).map { it?.toOffice() }
  }

  override suspend fun syncOffices(): Result<Unit, DataError.Remote> {
    val prefs = dataStore.data.first()
    val lastSync = prefs[keyLastSync] ?: 0L
    val now = getCurrentTimeMillis()

    if (now - lastSync < SERVER_FETCH_INTERVAL_MS) {
      return Result.Success(Unit)
    }
    return refreshOffices()
  }

  override suspend fun refreshOffices(): Result<Unit, DataError.Remote> {
    val currentLocation = locationService.getCurrentLocation()

    val bboxString = if (currentLocation != null) {
      val bbox = GeoUtil.calculateBBox(currentLocation, SERVER_FETCH_RADIUS_KM)
      GeoUtil.toBBoxString(bbox)
    } else null

    return when (val result = remoteOfficeDataSource.getOffices(bboxString)) {
      is Result.Success -> {
        officeDao.replaceAll(result.data.map { it.toEntity() })
        dataStore.edit { it[keyLastSync] = getCurrentTimeMillis() }
        Result.Success(Unit)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun refreshOffice(id: Int): Result<Unit, DataError.Remote> {
    return when (val result = remoteOfficeDataSource.getOffice(id)) {
      is Result.Success -> {
        officeDao.upsertOffices(listOf(result.data.toEntity()))
        Result.Success(Unit)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }
}