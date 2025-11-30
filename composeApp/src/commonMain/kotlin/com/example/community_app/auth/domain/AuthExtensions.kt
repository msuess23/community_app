package com.example.community_app.auth.domain

import kotlinx.coroutines.flow.first

suspend fun AuthRepository.getUserIdOrNull(): Int? {
  val state = authState.first()
  return if (state is AuthState.Authenticated) state.user.id else null
}

suspend fun AuthRepository.requireUserId(): Int {
  return getUserIdOrNull() ?: throw IllegalStateException("User not authenticated")
}