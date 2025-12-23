package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.ticket.domain.model.EditableImage

class AddLocalImageUseCase(
  private val fileStorage: FileStorage
) {
  suspend operator fun invoke(tempPath: String): EditableImage {
    val fileName = fileStorage.moveFromTemp(tempPath)
    val fullPath = fileStorage.getFullPath(fileName)
    return EditableImage(
      uri = fullPath,
      isLocal = true,
      id = fullPath
    )
  }
}