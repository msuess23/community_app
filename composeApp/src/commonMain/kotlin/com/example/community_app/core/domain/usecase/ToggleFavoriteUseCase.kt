package com.example.community_app.core.domain.usecase

import com.example.community_app.core.data.local.favorite.FavoriteDao
import com.example.community_app.core.data.local.favorite.FavoriteEntity
import com.example.community_app.core.data.local.favorite.FavoriteType

class ToggleFavoriteUseCase(
  private val favoriteDao: FavoriteDao
) {
  suspend operator fun invoke(itemId: Int, type: FavoriteType, isFavorite: Boolean) {
    val entity = FavoriteEntity(itemId, type)

    if (isFavorite) {
      favoriteDao.addFavorite(entity)
    } else {
      favoriteDao.removeFavorite(entity)
    }
  }
}