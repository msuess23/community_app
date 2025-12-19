package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.domain.model.Address
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.ticket.domain.EditableImage
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SaveDraftUseCase(
  private val ticketRepository: TicketRepository,
  private val fetchUserLocationUseCase: FetchUserLocationUseCase
) {
  data class Params(
    val draftId: Long?,
    val title: String,
    val description: String,
    val category: TicketCategory,
    val officeId: Int?,
    val visibility: TicketVisibility,
    val images: List<EditableImage>,
    val useCurrentLocation: Boolean
  )

  operator fun invoke(params: Params): Flow<OperationResult> = flow {
    emit(OperationResult.Loading)

    val address: Address? = if (params.useCurrentLocation) {
      fetchUserLocationUseCase().location?.let {
        Address(latitude = it.latitude, longitude = it.longitude)
      }
    } else null

    val imageFileNames = params.images.filter { it.isLocal }.map {
      getFileNameFromPath(it.uri)
    }

    val draft = TicketDraft(
      id = params.draftId ?: 0L,
      title = params.title,
      description = params.description,
      category = params.category,
      officeId = params.officeId,
      visibility = params.visibility,
      images = imageFileNames,
      address = address,
      lastModified = getCurrentTimeMillis().toString()
    )
    ticketRepository.saveDraft(draft)
    emit(OperationResult.Success)
  }
}