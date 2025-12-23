package com.example.community_app.geocoding.domain.usecase

import com.example.community_app.core.domain.Result
import com.example.community_app.geocoding.presentation.AddressSuggestion
import com.example.community_app.geocoding.presentation.AddressSuggestionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class GetAddressSuggestionsUseCase(
  private val getHomeAddressUseCase: GetHomeAddressUseCase,
  private val getAddressHistoryUseCase: GetAddressHistoryUseCase,
  private val searchAddressUseCase: SearchAddressUseCase
) {
  operator fun invoke(query: String): Flow<List<AddressSuggestion>> {
    val apiFlow = flow {
      if (query.isNotBlank()) {
        val result = searchAddressUseCase(query)
        if (result is Result.Success) {
          emit(result.data)
        } else {
          emit(emptyList())
        }
      } else {
        emit(emptyList())
      }
    }

    return combine(
      getHomeAddressUseCase(),
      getAddressHistoryUseCase(),
      apiFlow
    ) { home, history, apiResults ->
      val homeMatches = if (home != null) {
        if (query.isBlank() ||
          home.getUiLine1().contains(query, ignoreCase = true) ||
          home.city?.contains(query, ignoreCase = true) == true
        ) {
          listOf(AddressSuggestion(home, AddressSuggestionType.HOME))
        } else emptyList()
      } else emptyList()

      val historyMatches = if (query.isBlank()) {
        history.map { AddressSuggestion(it, AddressSuggestionType.HISTORY) }
      } else {
        history.filter {
          it.getUiLine1().contains(query, ignoreCase = true) ||
              it.city?.contains(query, ignoreCase = true) == true
        }.map { AddressSuggestion(it, AddressSuggestionType.HISTORY) }
      }

      val apiMatches = apiResults.map {
        AddressSuggestion(it, AddressSuggestionType.API)
      }

      (homeMatches + historyMatches + apiMatches)
        .distinctBy { "${it.address.latitude},${it.address.longitude}" }
    }
  }
}