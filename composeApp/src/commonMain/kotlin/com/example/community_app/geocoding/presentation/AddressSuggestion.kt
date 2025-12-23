package com.example.community_app.geocoding.presentation

import com.example.community_app.geocoding.domain.model.Address

enum class AddressSuggestionType {
  HISTORY,
  API,
  HOME
}

data class AddressSuggestion(
  val address: Address,
  val type: AddressSuggestionType
)