package com.example.community_app.ticket.data.local.draft

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDraftDao {
  @Transaction
  @Query("SELECT * FROM ticket_drafts")
  fun getDrafts(): Flow<List<TicketDraftWithImages>>

  @Transaction
  @Query("SELECT * FROM ticket_drafts WHERE id = :id")
  suspend fun getDraftById(id: Long): TicketDraftWithImages?

  @Upsert
  suspend fun upsertDraftBase(draft: TicketDraftEntity): Long

  @Insert
  suspend fun insertImages(images: List<TicketDraftImageEntity>)

  @Query("DELETE FROM ticket_draft_images WHERE draftId = :draftId")
  suspend fun deleteImagesForDraft(draftId: Long)

  @Transaction
  suspend fun upsertDraftFull(
    draft: TicketDraftEntity,
    images: List<String>
  ): Long {
    val id = upsertDraftBase(draft)
    deleteImagesForDraft(id)
    if (images.isNotEmpty()) {
      insertImages(images.map {
        TicketDraftImageEntity(draftId = id, localUri = it)
      })
    }
    return id
  }

  @Query("DELETE FROM ticket_drafts WHERE id = :id")
  suspend fun deleteDraft(id: Long)

  @Query("DELETE FROM ticket_drafts")
  suspend fun clearAll()
}