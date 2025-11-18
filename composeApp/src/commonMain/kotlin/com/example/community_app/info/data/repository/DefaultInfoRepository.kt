package com.example.community_app.info.data.repository

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.map
import com.example.community_app.info.data.mappers.toInfo
import com.example.community_app.info.data.network.RemoteInfoDataSource
import com.example.community_app.info.domain.Info
import com.example.community_app.info.domain.InfoRepository

class DefaultInfoRepository(
  private val remoteInfoDataSource: RemoteInfoDataSource
): InfoRepository {
  override suspend fun getInfos(): Result<List<Info>, DataError.Remote> {
    return remoteInfoDataSource.getInfos().map { dto ->
      dto.map { it.toInfo() }
    }
  }
}