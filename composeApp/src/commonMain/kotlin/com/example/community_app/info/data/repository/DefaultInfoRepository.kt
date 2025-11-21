package com.example.community_app.info.data.repository

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.map
import com.example.community_app.info.data.local.InfoDao
import com.example.community_app.info.data.mappers.toEntity
import com.example.community_app.info.data.mappers.toInfo
import com.example.community_app.info.data.network.RemoteInfoDataSource
import com.example.community_app.info.domain.Info
import com.example.community_app.info.domain.InfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultInfoRepository(
  private val remoteInfoDataSource: RemoteInfoDataSource,
  private val infoDao: InfoDao
): InfoRepository {
  override fun getInfos(): Flow<List<Info>> {
    return infoDao.getInfos().map { entities ->
      entities.map { it.toInfo() }
    }
  }

  override suspend fun refreshInfos(): Result<Unit, DataError.Remote> {
    return when (val result = remoteInfoDataSource.getInfos()) {
      is Result.Success -> {
        try {
          val entities = result.data.map { it.toEntity() }
          infoDao.replaceAll(entities)
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
}