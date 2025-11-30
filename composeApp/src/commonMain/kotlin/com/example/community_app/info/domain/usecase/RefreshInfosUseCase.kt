package com.example.community_app.info.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.info.domain.InfoRepository

class RefreshInfosUseCase(
  private val repository: InfoRepository
) {
  suspend operator fun invoke(): Result<Unit, DataError.Remote> {
    return repository.refreshInfos()
  }
}