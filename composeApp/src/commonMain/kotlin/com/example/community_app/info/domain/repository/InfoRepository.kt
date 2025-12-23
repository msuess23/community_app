package com.example.community_app.info.domain.repository

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.InfoStatusDto
import com.example.community_app.info.domain.model.Info
import kotlinx.coroutines.flow.Flow

interface InfoRepository {
  fun getInfos(): Flow<List<Info>>
  fun getInfo(id: Int): Flow<Info?>

  suspend fun refreshInfos(force: Boolean = false): Result<Unit, DataError.Remote>
  suspend fun refreshInfo(id: Int): Result<Unit, DataError.Remote>

  suspend fun getStatusHistory(id: Int): Result<List<InfoStatusDto>, DataError.Remote>
  suspend fun getCurrentStatus(id: Int): Result<InfoStatusDto?, DataError.Remote>

  suspend fun toggleFavorite(infoId: Int, isFavorite: Boolean)
}