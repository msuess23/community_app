package com.example.community_app.profile.domain.repository

import kotlinx.coroutines.flow.first

suspend fun UserRepository.getUserIdOrNull(): Int? {
  return getUser().first()?.id
}

suspend fun UserRepository.requireUserId(): Int {
  return getUserIdOrNull() ?: throw IllegalStateException("User not authenticated or profile not loaded")
}