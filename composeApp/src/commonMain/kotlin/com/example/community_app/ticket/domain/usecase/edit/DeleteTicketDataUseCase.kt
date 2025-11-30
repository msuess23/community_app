package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.ticket.domain.EditableImage
import com.example.community_app.ticket.domain.TicketRepository

class DeleteTicketDataUseCase(
  private val ticketRepository: TicketRepository,
  private val fileStorage: FileStorage
) {
  suspend operator fun invoke(
    ticketId: Int?,
    draftId: Long?,
    images: List<EditableImage>
  ): Result<Unit, DataError> {

    return if (draftId != null) {
      images.filter { it.isLocal }.forEach {
        val name = getFileNameFromPath(it.uri)
        fileStorage.deleteImage(name)
      }
      ticketRepository.deleteDraft(draftId)
      Result.Success(Unit)
    } else if (ticketId != null) {
      val result = ticketRepository.deleteTicket(ticketId)
      if (result is Result.Error) Result.Error(result.error) else Result.Success(Unit)
    } else {
      Result.Success(Unit)
    }
  }
}