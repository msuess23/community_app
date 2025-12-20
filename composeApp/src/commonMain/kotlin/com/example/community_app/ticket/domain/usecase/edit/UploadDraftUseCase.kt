package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.domain.Result
import com.example.community_app.geocoding.domain.Address
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.ticket.domain.EditableImage
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UploadDraftUseCase(
  private val ticketRepository: TicketRepository,
  private val fileStorage: FileStorage,
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
      val locResult = fetchUserLocationUseCase()
      locResult.location?.let { Address(latitude = it.latitude, longitude = it.longitude) }
    } else null

    val imageFileNames = params.images.filter { it.isLocal }.map { getFileNameFromPath(it.uri) }

    val draft = TicketDraft(
      id = params.draftId ?: 0L,
      title = params.title,
      description = params.description,
      category = params.category,
      officeId = params.officeId ?: 1,
      visibility = params.visibility,
      images = imageFileNames,
      address = address,
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