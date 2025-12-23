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
  @Query("SELECT * FROM ticket_drafts WHERE userId = :userId ORDER BY lastModified DESC")
  fun getDrafts(userId: Int): Flow<List<TicketDraftWithImages>>

  @Transaction
  @Query("SELECT * FROM ticket_drafts WHERE id = :id AND userId = :userId")
  suspend fun getDraftById(id: Long, userId: Int): TicketDraftWithImages?

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
    val resultId = upsertDraftBase(draft)
    val finalId = if (resultId == -1L) draft.id else resultId

    deleteImagesForDraft(finalId)
    if (images.isNotEmpty()) {
      insertImages(images.map {
        TicketDraftImageEntity(draftId = finalId, localUri = it)
      })
    }
    return finalId
  }

  @Query("DELETE FROM ticket_drafts WHERE id = :id")
  suspend fun deleteDraft(id: Long)

  @Query("DELETE FROM ticket_drafts WHERE userId = :userId")
  suspend fun deleteDraftsForUser(userId: Int)
}