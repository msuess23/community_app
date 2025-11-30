package com.example.community_app.ticket.domain.usecase.detail

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.MediaTargetType

class SyncTicketImagesUseCase(
  private val mediaRepository: MediaRepository,
  private val fileStorage: FileStorage
) {
  suspend operator fun invoke(
    ticketId: Int,
    isOwner: Boolean
  ): Result<List<String>, DataError.Remote> {
    val result = mediaRepository.getMediaList(
      targetType = MediaTargetType.TICKET,
      targetId = ticketId
    )

    return if (result is Result.Success) {
      val imagePaths = result.data.map { mediaDto ->
        val fileName = "ticket_${ticketId}_${mediaDto.id}.jpg"

        if (fileStorage.exists(fileName)) {
          fileStorage.getFullPath(fileName)
        } else if (isOwner) {
          val dlResult = mediaRepository.downloadMedia(
            url = mediaDto.url,
            saveToFileName = fileName
          )

          if (dlResult is Result.Success) {
            fileStorage.getFullPath(fileName)
          } else {
            "$BASE_URL${mediaDto.url}"
          }
        } else {
          "$BASE_URL${mediaDto.url}"
        }
      }
      Result.Success(imagePaths)
    } else {
      Result.Error((result as Result.Error).error)
    }
  }
}