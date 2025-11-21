package com.example.community_app.info.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InfoDao {
  @Upsert
  suspend fun upsertInfos(infos: List<InfoEntity>)

  @Query("SELECT * FROM infos")
  fun getInfos(): Flow<List<InfoEntity>>

  @Query("SELECT * FROM infos WHERE id = :id")
  fun getInfoById(id: Int): Flow<InfoEntity?>

  @Query("DELETE FROM infos")
  suspend fun clearAll()

  @Transaction
  suspend fun replaceAll(infos: List<InfoEntity>) {
    clearAll()
    upsertInfos(infos)
  }
}