package com.example.community_app.ticket.data.local.draft

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDraftDao {
  @Query("SELECT * FROM ticket_drafts ORDER BY lastModified DESC")
  fun getDrafts(): Flow<List<TicketDraftEntity>>

  @Query("SELECT * FROM ticket_drafts WHERE id = :id")
  suspend fun getDraftById(id: Long): TicketDraftEntity?

  @Upsert
  suspend fun upsertDraft(draft: TicketDraftEntity): Long

  @Query("DELETE FROM ticket_drafts WHERE id = :id")
  suspend fun deleteDraft(id: Long)

  @Query("DELETE FROM ticket_drafts")
  suspend fun clearAll()
}