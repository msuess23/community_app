package com.example.community_app.auth.domain

import com.example.community_app.dto.UserDto

sealed interface AuthState {
  data object Loading : AuthState
  data object Unauthenticated : AuthState
  data object Authenticated : AuthState
}