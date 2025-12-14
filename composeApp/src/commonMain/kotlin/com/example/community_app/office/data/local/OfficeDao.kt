package com.example.community_app.office.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficeDao {
  @Upsert
  suspend fun upsertOffices(offices: List<OfficeEntity>)

  @Query("SELECT * FROM offices")
  fun getOffices(): Flow<List<OfficeEntity>>

  @Query("SELECT * FROM offices WHERE id = :id")
  fun getOfficeById(id: Int): Flow<OfficeEntity?>

  @Query("DELETE FROM offices")
  suspend fun clearAll()

  @Transaction
  suspend fun replaceAll(offices: List<OfficeEntity>) {
    clearAll()
    upsertOffices(offices)
  }
}