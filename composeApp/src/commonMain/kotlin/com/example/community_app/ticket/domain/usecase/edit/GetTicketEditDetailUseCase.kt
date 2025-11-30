package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.ticket.domain.EditableImage
import com.example.community_app.ticket.domain.TicketEditDetails
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.MediaTargetType
import com.example.community_app.util.TicketCategory
import kotlinx.coroutines.flow.first

class GetTicketEditDetailsUseCase(
  private val ticketRepository: TicketRepository,
  private val mediaRepository: MediaRepository
) {
  suspend operator fun invoke(ticketId: Int?, draftId: Long?): Result<TicketEditDetails, DataError> {
    return when {
      ticketId != null -> loadTicket(ticketId)
      draftId != null -> loadDraft(draftId)
      else -> Result.Success(TicketEditDetails(isDraft = true))
    }
  }

  private suspend fun loadTicket(id: Int): Result<TicketEditDetails, DataError> {
    val ticket = ticketRepository.getTicket(id).first()
      ?: return Result.Error(DataError.Local.UNKNOWN)

    val mediaResult = mediaRepository.getMediaList(
      targetType = MediaTargetType.TICKET,
      targetId = id
    )

    val images = if (mediaResult is Result.Success) {
      mediaResult.data.map {
        EditableImage(
          uri = "$BASE_URL${it.url}",
          isLocal = false,
          id = it.id.toString()
        )
      }
    } else emptyList()

    return Result.Success(
      TicketEditDetails(
        isDraft = false,
        ticketId = id,
        title = ticket.title,
        description = ticket.description ?: "",
        category = ticket.category,
        visibility = ticket.visibility,
        officeId = ticket.officeId,
        images = images,
        coverImageUri = ticket.imageUrl
      )
    )
  }

  private suspend fun loadDraft(id: Long): Result<TicketEditDetails, DataError> {
    val draft = ticketRepository.getDraft(id)
      ?: return Result.Error(DataError.Local.UNKNOWN)

    val images = draft.images.map { path ->
      EditableImage(uri = path, isLocal = true, id = path)
    }

    return Result.Success(
      TicketEditDetails(
        isDraft = true,
        draftId = id,
        title = draft.title,
        description = draft.description ?: "",
        category = draft.category ?: TicketCategory.OTHER,
        visibility = draft.visibility,
        officeId = draft.officeId,
        images = images,
        coverImageUri = images.firstOrNull()?.uri
      )
    )
  }
}