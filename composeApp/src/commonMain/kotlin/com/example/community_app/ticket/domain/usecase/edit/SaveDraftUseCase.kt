package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketEditInput
import com.example.community_app.ticket.domain.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SaveDraftUseCase(
  private val ticketRepository: TicketRepository
) {
  operator fun invoke(draftId: Long?, input: TicketEditInput): Flow<OperationResult> = flow {
    emit(OperationResult.Loading)

    val imageFileNames = input.images.filter { it.isLocal }.map {
      getFileNameFromPath(it.uri)
    }

    val draft = TicketDraft(
      id = draftId ?: 0L,
      title = input.title,
      description = input.description,
      category = input.category,
      officeId = input.officeId,
      visibility = input.visibility,
      images = imageFileNames,
      address = input.address,
      lastModified = getCurrentTimeMillis().toString()
    )
    ticketRepository.saveDraft(draft)
    emit(OperationResult.Success)
  }
}