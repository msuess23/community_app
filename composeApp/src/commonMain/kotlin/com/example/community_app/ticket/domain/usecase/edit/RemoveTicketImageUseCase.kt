package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.ticket.domain.EditableImage
import com.example.community_app.util.MediaTargetType

class RemoveTicketImageUseCase(
  private val mediaRepository: MediaRepository,
  private val fileStorage: FileStorage
) {
  suspend operator fun invoke(
    image: EditableImage,
    ticketId: Int?,
    isDraft: Boolean
  ) {
    if (image.isLocal) {
      val fileName = getFileNameFromPath(image.uri)
      fileStorage.deleteImage(fileName)
    } else if (!isDraft && ticketId != null) {
      mediaRepository.deleteMedia(
        targetType = MediaTargetType.TICKET,
        targetId = ticketId,
        mediaId = image.id.toInt()
      )
    }
  }
}