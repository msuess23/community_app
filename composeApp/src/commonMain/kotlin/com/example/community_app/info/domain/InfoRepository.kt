package com.example.community_app.info.domain

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface InfoRepository {
  fun getInfos(): Flow<List<Info>>

  suspend fun refreshInfos(): Result<Unit, DataError.Remote>
}