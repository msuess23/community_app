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

  @Query("SELECT itemId FROM favorites WHERE userId = :userId AND type = :type")
  fun getFavoriteIds(userId: Int, type: FavoriteType): Flow<List<Int>>

  @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND itemId = :itemId AND type = :type)")
  suspend fun isFavorite(userId: Int, itemId: Int, type: FavoriteType): Boolean

  @Delete
  suspend fun removeFavorite(favorite: FavoriteEntity)

  @Query("DELETE FROM favorites WHERE userId = :userId")
  suspend fun clearFavoritesForUser(userId: Int)
}