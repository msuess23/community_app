package com.example.community_app.office.data.repository

import com.example.community_app.core.data.sync.SyncManager
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.office.data.local.OfficeDao
import com.example.community_app.office.data.mappers.toEntity
import com.example.community_app.office.data.mappers.toOffice
import com.example.community_app.office.data.network.RemoteOfficeDataSource
import com.example.community_app.office.domain.model.Office
import com.example.community_app.office.domain.repository.OfficeRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultOfficeRepository(
  private val remoteOfficeDataSource: RemoteOfficeDataSource,
  private val officeDao: OfficeDao,
  private val syncManager: SyncManager
) : OfficeRepository {
  override fun getOffices(): Flow<List<Office>> {
    return officeDao.getOffices().map { entities ->
      entities.map { it.toOffice() }
    }
  }

  override fun getOffice(id: Int): Flow<Office?> {
    return officeDao.getOfficeById(id).map { it?.toOffice() }
  }

  override suspend fun refreshOffices(force: Boolean): Result<Unit, DataError.Remote> = coroutineScope {
    val decision = syncManager.checkSyncStatus(
      featureKey = "office",
      forceRefresh = force
    )

    if (!decision.shouldFetch) {
      return@coroutineScope Result.Success(Unit)
    }

    when(val result = remoteOfficeDataSource.getOffices(bbox = decision.bboxString)) {
      is Result.Success -> {
        val entities = result.data.map { it.toEntity() }
        officeDao.replaceAll(entities)
        syncManager.updateSyncSuccess("office", decision.currentLocation)
        Result.Success(Unit)
      }
      is Result.Error -> {
        Result.Error(result.error)
      }
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