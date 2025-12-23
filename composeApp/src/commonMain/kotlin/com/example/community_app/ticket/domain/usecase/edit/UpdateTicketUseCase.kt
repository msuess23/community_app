package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.media.domain.repository.MediaRepository
import com.example.community_app.ticket.domain.model.EditableImage
import com.example.community_app.ticket.domain.model.TicketEditInput
import com.example.community_app.ticket.domain.repository.TicketRepository
import com.example.community_app.util.MediaTargetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateTicketUseCase(
  private val ticketRepository: TicketRepository,
  private val mediaRepository: MediaRepository,
  private val fileStorage: FileStorage
) {
  operator fun invoke(
    ticketId: Int,
    input: TicketEditInput,
    imagesToDelete: Set<EditableImage>,
    coverImageUri: String?
  ): Flow<OperationResult> = flow {
    emit(OperationResult.Loading)

    imagesToDelete.forEach { img ->
      if (img.isLocal) {
        fileStorage.deleteImage(getFileNameFromPath(img.uri))
      } else {
        mediaRepository.deleteMedia(
          targetType = MediaTargetType.TICKET,
          targetId = ticketId,
          mediaId = img.id.toInt()
        )
      }
    }

    val updateResult = ticketRepository.updateTicket(
      id = ticketId,
      title = input.title,
      description = input.description,
      category = input.category,
      officeId = input.officeId,
      address = input.address,
      visibility = input.visibility
    )

    if (updateResult is Result.Error) {
      emit(OperationResult.Error(updateResult.error.toUiText()))
      return@flow
    }

    val localImages = input.images.filter { it.isLocal }
    for (img in localImages) {
      val fileName = getFileNameFromPath(img.uri)
      val uploadResult = mediaRepository.uploadMedia(
        targetType = MediaTargetType.TICKET,
        targetId = ticketId,
        fileName = fileName
      )

      if (uploadResult is Result.Success) {
        fileStorage.deleteImage(fileName)
        if (img.uri == coverImageUri) {
          mediaRepository.setCover(uploadResult.data.id)
        }
      } else {
        emit(OperationResult.Error((uploadResult as Result.Error).error.toUiText()))
        return@flow
      }
    }

    val remoteCover = input.images.find { it.uri == coverImageUri && !it.isLocal }
    if (remoteCover != null) {
      mediaRepository.setCover(remoteCover.id.toInt())
    }

    ticketRepository.refreshTicket(ticketId)

    emit(OperationResult.Success)
  }
}