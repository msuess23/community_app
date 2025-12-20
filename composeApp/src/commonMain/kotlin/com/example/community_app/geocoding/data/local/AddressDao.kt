package com.example.community_app.geocoding.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
  @Query("SELECT * FROM address_history WHERE userId = :userId AND type IS NULL ORDER BY lastUsedAt DESC LIMIT 20")
  fun getHistory(userId: Int): Flow<List<AddressEntity>>

  @Query("SELECT * FROM address_history WHERE userId = :userId AND type = :type LIMIT 1")
  fun getAddressByType(userId: Int, type: String): Flow<AddressEntity?>

  @Query("UPDATE address_history SET type = NULL WHERE userId = :userId AND type = 'HOME'")
  suspend fun demoteHomeAddress(userId: Int)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAddress(address: AddressEntity)

  @Query("SELECT * FROM address_history WHERE userId = :userId AND latitude = :lat AND longitude = :lon AND type IS NULL LIMIT 1")
  suspend fun findHistoryEntryByLocation(userId: Int, lat: Double, lon: Double): AddressEntity?

  @Query("DELETE FROM address_history WHERE userId = :userId")
  suspend fun deleteAllForUser(userId: Int)
}