package com.example.community_app.info.domain

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface InfoRepository {
  fun getInfos(): Flow<List<Info>>
  fun getInfo(id: Int): Flow<Info?>

  suspend fun refreshInfos(): Result<Unit, DataError.Remote>
  suspend fun refreshInfo(id: Int): Result<Unit, DataError.Remote>
}