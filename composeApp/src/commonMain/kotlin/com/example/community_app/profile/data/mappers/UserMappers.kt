package com.example.community_app.profile.data.mappers

import com.example.community_app.dto.UserDto
import com.example.community_app.profile.data.local.CURRENT_USER_ID
import com.example.community_app.profile.data.local.UserEntity
import com.example.community_app.profile.domain.User

fun UserDto.toEntity(): UserEntity {
  return UserEntity(
    id = CURRENT_USER_ID,
    userId = id,
    email = email,
    displayName = displayName
  )
}

fun UserEntity.toUser(): User {
  return User(
    id = userId,
    email = email,
    displayName = displayName ?: ""
  )
}