package com.example.community_app.auth.domain

sealed interface AuthState {
  data object Loading : AuthState
  data object Unauthenticated : AuthState
  data object Authenticated : AuthState
}