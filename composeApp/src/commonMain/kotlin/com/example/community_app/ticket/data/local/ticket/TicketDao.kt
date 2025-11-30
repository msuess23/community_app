package com.example.community_app.ticket.data.local.ticket

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
  @Upsert
  suspend fun upsertTickets(tickets: List<TicketEntity>)

  @Query("SELECT * FROM tickets")
  fun getTickets(): Flow<List<TicketEntity>>

  @Query("SELECT * FROM tickets WHERE creatorUserId != :userId")
  fun getCommunityTickets(userId: Int): Flow<List<TicketEntity>>

  @Query("SELECT * FROM tickets WHERE creatorUserId = :userId")
  fun getUserTickets(userId: Int): Flow<List<TicketEntity>>

  @Query("SELECT * FROM tickets WHERE id = :id")
  fun getTicketById(id: Int): Flow<TicketEntity?>

  @Query("DELETE FROM tickets")
  suspend fun clearAll()

  @Transaction
  suspend fun replaceAll(tickets: List<TicketEntity>) {
    clearAll()
    upsertTickets(tickets)
  }
}