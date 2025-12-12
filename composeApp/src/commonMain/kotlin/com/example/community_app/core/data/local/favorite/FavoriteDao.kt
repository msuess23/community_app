package com.example.community_app.core.data.local.favorite

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addFavorite(favorite: FavoriteEntity)

  @Delete
  suspend fun removeFavorite(favorite: FavoriteEntity)

  @Query("SELECT itemId FROM favorites WHERE type = :type")
  fun getFavoriteIds(type: FavoriteType): Flow<List<Int>>

  @Query("SELECT COUNT(*) > 0 FROM favorites WHERE itemId = :itemId AND type = :type")
  suspend fun isFavorite(itemId: Int, type: FavoriteType): Boolean
}