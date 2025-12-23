package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.core.presentation.helpers.UiText

sealed interface OperationResult {
  data object Loading : OperationResult
  data object Success : OperationResult
  data class Error(val message: UiText) : OperationResult
}