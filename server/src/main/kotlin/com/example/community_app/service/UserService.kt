package com.example.community_app.service

import com.example.community_app.dto.UserDto
import com.example.community_app.dto.UserUpdateDto
import com.example.community_app.errors.NotFoundException
import com.example.community_app.repository.DefaultUserRepository
import com.example.community_app.repository.UserRepository
import io.ktor.server.auth.jwt.*

class UserService(
  private val repo: UserRepository
) {
  suspend fun getMe(principal: JWTPrincipal): UserDto {
    val userId = principal.subject?.toIntOrNull() ?: throw NotFoundException("User not found")
    val u = repo.findById(userId) ?: throw NotFoundException("User not found")
    return UserDto(u.id, u.email, u.displayName, u.role, u.officeId)
  }

  suspend fun updateMe(principal: JWTPrincipal, dto: UserUpdateDto): UserDto {
    val userId = principal.subject?.toIntOrNull() ?: throw NotFoundException("User not found")
    val updated = repo.updateProfile(userId, dto.displayName) ?: throw NotFoundException("User not found")
    return UserDto(updated.id, updated.email, updated.displayName, updated.role, updated.officeId)
  }

  companion object {
    fun default(): UserService = UserService(DefaultUserRepository)
  }
}
