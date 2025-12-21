package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.office.domain.Office
import com.example.community_app.office.domain.OfficeRepository
import com.example.community_app.ticket.domain.EditableImage
import com.example.community_app.ticket.domain.TicketEditDetails
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.MediaTargetType
import com.example.community_app.util.TicketCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

data class TicketEditData(
  val details: TicketEditDetails? = null,
  val offices: List<Office> = emptyList(),
  val syncError: DataError? = null
)

class GetTicketEditDetailsUseCase(
  private val ticketRepository: TicketRepository,
  private val mediaRepository: MediaRepository,
  private val officeRepository: OfficeRepository
) {
  operator fun invoke(ticketId: Int?, draftId: Long?): Flow<Result<TicketEditData, DataError>> = flow {
    val detailsResult = when {
      ticketId != null -> loadTicket(ticketId)
      draftId != null -> loadDraft(draftId)
      else -> Result.Success(TicketEditDetails(isDraft = true))
    }

    if (detailsResult is Result.Error) {
      emit(Result.Error(detailsResult.error))
      return@flow
    }
    val details = (detailsResult as Result.Success).data

    val refreshResult = officeRepository.refreshOffices(force = true)
    var syncError: DataError? = (refreshResult as? Result.Error)?.error

    if (details.officeId != null) {
      val currentOffices = officeRepository.getOffices().first()
      val isMissing = currentOffices.none { it.id == details.officeId }

      if (isMissing) {
        val specificRefreshResult = officeRepository.refreshOffice(details.officeId)
        if (specificRefreshResult is Result.Error) {
          if (syncError == null) syncError = specificRefreshResult.error
        }
      }
    }

    emitAll(
      officeRepository.getOffices().map { offices ->
        Result.Success(
          TicketEditData(
            details = details,
            offices = offices,
            syncError = syncError
          )
        )
      }
    )
  }

  private suspend fun loadTicket(id: Int): Result<TicketEditDetails, DataError> {
    val ticket = ticketRepository.getTicket(id).firstOrNull()
      ?: return Result.Error(DataError.Local.UNKNOWN)

    val mediaResult = mediaRepository.getMediaList(MediaTargetType.TICKET, id)
    val images = if (mediaResult is Result.Success) {
      mediaResult.data.map { EditableImage(
        uri = "$BASE_URL${it.url}",
        isLocal = false,
        id = it.id.toString()
      ) }
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
        address = ticket.address,
        images = images,
        coverImageUri = ticket.imageUrl
      )
    )
  }

  private suspend fun loadDraft(id: Long): Result<TicketEditDetails, DataError> {
    val draft = ticketRepository.getDraft(id)
      ?: return Result.Error(DataError.Local.UNKNOWN)

    val images = draft.images.map { EditableImage(it, true, it) }

    return Result.Success(
      TicketEditDetails(
        isDraft = true,
        draftId = id,
        title = draft.title,
        description = draft.description ?: "",
        category = draft.category ?: TicketCategory.OTHER,
        visibility = draft.visibility,
        officeId = draft.officeId,
        address = draft.address,
        images = images,
        coverImageUri = images.firstOrNull()?.uri
      )
    )
  }
}