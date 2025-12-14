package com.example.community_app.core.data.background

import com.example.community_app.core.domain.usecase.CheckStatusUpdatesUseCase
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BackgroundManager : KoinComponent {
  private val checkStatusUpdates: CheckStatusUpdatesUseCase by inject()
  private val scope = MainScope()

  fun performBackgroundFetch(completion: (Boolean) -> Unit) {
    scope.launch {
      try {
        checkStatusUpdates()
        completion(true)
      } catch (e: Exception) {
        e.printStackTrace()
        completion(false)
      }
    }
  }
}