package com.example.community_app.service

import com.example.community_app.config.JwtConfig
import com.example.community_app.dto.*
import com.example.community_app.repository.UserRepository
import com.example.community_app.util.PasswordUtil

class AuthService(private val repo: UserRepository = UserRepository()) {

  fun register(dto: RegisterDto): TokenResponse {
    val email = dto.email.trim().lowercase()
    require(email.contains("@")) { "Ungültige E-Mail-Adresse" }
    require(dto.password.length >= 8) { "Passwort zu kurz" }

    check(repo.findByEmail(email) == null) { "E-Mail bereits vergeben" }

    val hash = PasswordUtil.hash(dto.password)
    val user = repo.create(email, dto.displayName, hash)
    val token = JwtConfig.generateToken(user.id.value, user.email)

    return TokenResponse(accessToken = token)
  }

  fun login(dto: LoginDto): TokenResponse {
    val user = repo.findByEmail(dto.email.lowercase())
      ?: throw IllegalArgumentException("Ungültige Anmeldedaten")

    check(PasswordUtil.verify(dto.password, user.passwordHash)) { "Ungültige Anmeldedaten" }
    val token = JwtConfig.generateToken(user.id.value, user.email)

    return TokenResponse(accessToken = token)
  }

  fun me(userId: Int): MeDto {
    val user = repo.findById(userId) ?: throw IllegalArgumentException("Benutzer nicht gefunden")
    return MeDto(user.id.value, user.email, user.displayName)
  }

  fun changePassword(userId: Int, dto: ChangePasswordDto) {
    val user = repo.findById(userId) ?: throw IllegalArgumentException("Benutzer nicht gefunden")
    check(PasswordUtil.verify(dto.oldPassword, user.passwordHash)) { "Altes Passwort falsch" }
    repo.updatePassword(userId, PasswordUtil.hash(dto.newPassword))
  }
}
