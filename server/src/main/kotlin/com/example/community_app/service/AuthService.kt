package com.example.community_app.service

import com.example.community_app.config.JwtConfig
import com.example.community_app.dto.*
import com.example.community_app.repository.UserRepository
import com.example.community_app.util.PasswordUtil

class AuthService(private val repo: UserRepository = UserRepository) {

  suspend fun register(dto: RegisterDto): String {
    val email = dto.email.trim().lowercase()
    require(email.contains("@")) { "Ungültige E-Mail-Adresse" }
    require(dto.password.length >= 8) { "Passwort zu kurz" }

    check(repo.findByEmail(email) == null) { "E-Mail bereits vergeben" }

    val hash = PasswordUtil.hash(dto.password)
    val user = repo.create(email, hash, dto.displayName)

    return JwtConfig.generateToken(user.id.value)
  }

  suspend fun login(dto: LoginDto): String {
    val user = repo.findByEmail(dto.email.lowercase())
      ?: throw IllegalArgumentException("Ungültige Anmeldedaten")

    check(PasswordUtil.verify(dto.password, user.passwordHash)) { "Ungültige Anmeldedaten" }

    return JwtConfig.generateToken(user.id.value)
  }
}
