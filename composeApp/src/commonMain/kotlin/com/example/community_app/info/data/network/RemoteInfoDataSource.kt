package com.example.community_app.info.data.network

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.InfoDto

interface RemoteInfoDataSource {
  suspend fun getInfos(): Result<List<InfoDto>, DataError.Remote>
  suspend fun getInfo(id: Int): Result<InfoDto, DataError.Remote>
}