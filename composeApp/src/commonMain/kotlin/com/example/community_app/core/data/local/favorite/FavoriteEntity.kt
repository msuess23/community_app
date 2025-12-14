package com.example.community_app.core.data.local.favorite

import androidx.room.Entity
import com.example.community_app.core.util.getCurrentTimeMillis

enum class FavoriteType {
  INFO, TICKET
}

@Entity(
  tableName = "favorites",
  primaryKeys = ["itemId", "type"]
)
data class FavoriteEntity(
  val itemId: Int,
  val type: FavoriteType,
  val createdAt: Long = getCurrentTimeMillis()
)