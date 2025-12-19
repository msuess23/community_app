package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.util.getFileNameFromPath

class DiscardLocalImagesUseCase(
  private val fileStorage: FileStorage
) {
  suspend operator fun invoke(uris: Set<String>) {
    uris.forEach { uri ->
      val fileName = getFileNameFromPath(uri)
      fileStorage.deleteImage(fileName)
    }
  }
}