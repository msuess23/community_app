package com.example.community_app.info.domain

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result

interface InfoRepository {
  suspend fun getInfos(): Result<List<Info>, DataError.Remote>
}