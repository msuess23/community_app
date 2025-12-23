package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.ticket.domain.model.TicketDraft
import com.example.community_app.ticket.domain.model.TicketEditInput
import com.example.community_app.ticket.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UploadDraftUseCase(
  private val ticketRepository: TicketRepository,
  private val fileStorage: FileStorage
) {
  operator fun invoke(draftId: Long?, input: TicketEditInput): Flow<OperationResult> = flow {
    emit(OperationResult.Loading)

    val imageFileNames = input.images.filter { it.isLocal }.map { getFileNameFromPath(it.uri) }

    val draft = TicketDraft(
      id = draftId ?: 0L,
      title = input.title,
      description = input.description,
      category = input.category,
      officeId = input.officeId ?: 1,
      visibility = input.visibility,
      images = imageFileNames,
      address = input.address,
      lastModified = ""
    )

    val result = ticketRepository.uploadDraft(draft)

    if (result is Result.Success) {
      imageFileNames.forEach { fileStorage.deleteImage(it) }
      emit(OperationResult.Success)
    } else {
      emit(OperationResult.Error((result as Result.Error).error.toUiText()))
    }
  }
}