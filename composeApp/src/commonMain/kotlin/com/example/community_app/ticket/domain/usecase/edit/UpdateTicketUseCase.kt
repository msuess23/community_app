package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.ticket.domain.EditableImage
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.util.MediaTargetType
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility

class UpdateTicketUseCase(
  private val ticketRepository: TicketRepository,
  private val mediaRepository: MediaRepository,
  private val fileStorage: FileStorage
) {
  data class Params(
    val ticketId: Int,
    val title: String,
    val description: String,
    val category: TicketCategory,
    val visibility: TicketVisibility,
    val officeId: Int?,
    val images: List<EditableImage>,
    val coverImageUri: String?
  )

  suspend operator fun invoke(params: Params): Result<Unit, DataError> {
    val updateResult = ticketRepository.updateTicket(
      id = params.ticketId,
      title = params.title,
      description = params.description,
      category = params.category,
      officeId = params.officeId,
      address = null,
      visibility = params.visibility
    )

    if (updateResult is Result.Error) {
      return Result.Error(updateResult.error)
    }

    val localImages = params.images.filter { it.isLocal }
    var uploadError: DataError? = null

    for (img in localImages) {
      val fileName = getFileNameFromPath(img.uri)

      val uploadResult = mediaRepository.uploadMedia(
        targetType = MediaTargetType.TICKET,
        targetId = params.ticketId,
        fileName = fileName
      )

      if (uploadResult is Result.Success) {
        fileStorage.deleteImage(fileName)

        if (img.uri == params.coverImageUri) {
          mediaRepository.setCover(uploadResult.data.id)
        }
      } else {
        uploadError = (uploadResult as Result.Error).error
      }
    }

    val remoteCover = params.images.find {
      it.uri == params.coverImageUri && !it.isLocal
    }
    if (remoteCover != null) {
      mediaRepository.setCover(remoteCover.id.toInt())
    }

    return if (uploadError != null) Result.Error(uploadError) else Result.Success(Unit)
  }
}