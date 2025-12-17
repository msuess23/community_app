package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.ticket.domain.Ticket
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketRepository

class UploadDraftUseCase(
  private val ticketRepository: TicketRepository,
  private val fileStorage: FileStorage
) {
  suspend operator fun invoke(draft: TicketDraft): Result<Ticket, DataError.Remote> {
    val result = ticketRepository.uploadDraft(draft)

    if (result is Result.Success) {
      draft.images.forEach { localPath ->
        val fileName = getFileNameFromPath(localPath)
        fileStorage.deleteImage(fileName)
      }
    }
    return result
  }
}