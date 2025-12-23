package com.example.community_app.info.domain.usecase

import com.example.community_app.info.domain.repository.InfoRepository

class ToggleInfoFavoriteUseCase(
  private val infoRepository: InfoRepository
) {
  suspend operator fun invoke(itemId: Int, isFavorite: Boolean) {
    infoRepository.toggleFavorite(itemId, isFavorite)
  }
}