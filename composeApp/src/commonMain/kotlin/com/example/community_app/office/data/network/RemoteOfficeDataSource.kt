package com.example.community_app.office.data.network

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.OfficeDto

interface RemoteOfficeDataSource {
  suspend fun getOffices(bbox: String? = null): Result<List<OfficeDto>, DataError.Remote>
  suspend fun getOffice(id: Int): Result<OfficeDto, DataError.Remote>
}