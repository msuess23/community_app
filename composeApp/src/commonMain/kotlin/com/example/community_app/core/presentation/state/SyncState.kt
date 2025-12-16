package com.example.community_app.core.presentation.state

import com.example.community_app.core.domain.DataError

data class SyncState(
  val isLoading: Boolean,
  val error: DataError? = null
)