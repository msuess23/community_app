package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.ticket.domain.EditableImage
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.util.MediaTargetType
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
    val imagesToDelete: Set<EditableImage>,
    val coverImageUri: String?
  )

  operator fun invoke(params: Params): Flow<OperationResult> = flow {
    emit(OperationResult.Loading)

    params.imagesToDelete.forEach { img ->
      if (img.isLocal) {
        fileStorage.deleteImage(getFileNameFromPath(img.uri))
      } else {
        mediaRepository.deleteMedia(
          targetType = MediaTargetType.TICKET,
          targetId = params.ticketId,
          mediaId = img.id.toInt()
        )
      }
    }

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
      emit(OperationResult.Error(updateResult.error.toUiText()))
      return@flow
    }

    val localImages = params.images.filter { it.isLocal }
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
        emit(OperationResult.Error((uploadResult as Result.Error).error.toUiText()))
        return@flow
      }
    }

    val remoteCover = params.images.find { it.uri == params.coverImageUri && !it.isLocal }
    if (remoteCover != null) {
      mediaRepository.setCover(remoteCover.id.toInt())
    }

    ticketRepository.refreshTicket(params.ticketId)

    emit(OperationResult.Success)
  }
}