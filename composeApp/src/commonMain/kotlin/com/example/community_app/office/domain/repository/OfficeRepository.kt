package com.example.community_app.office.domain.repository

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.office.domain.model.Office
import kotlinx.coroutines.flow.Flow

interface OfficeRepository {
  fun getOffices(): Flow<List<Office>>
  fun getOffice(id: Int): Flow<Office?>

  suspend fun refreshOffices(force: Boolean = false): Result<Unit, DataError.Remote>
  suspend fun refreshOffice(id: Int): Result<Unit, DataError.Remote>
}