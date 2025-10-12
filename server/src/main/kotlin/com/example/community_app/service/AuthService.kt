package com.example.community_app.service

import com.example.community_app.config.IssuedToken
import com.example.community_app.config.JwtConfig
import com.example.community_app.dto.*
import com.example.community_app.errors.*
import com.example.community_app.repository.DefaultUserRepository
import com.example.community_app.repository.UserRecord
import com.example.community_app.repository.UserRepository
import com.example.community_app.util.PasswordUtil
import com.example.community_app.util.TokenStore
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*

class AuthService(
  private val repo: UserRepository,
  private val emailService: EmailService,
  private val otpService: OtpService
) {

  suspend fun register(dto: RegisterDto): TokenResponse {
    val email = dto.email.trim().lowercase()
    validateEmail(email)
    validatePassword(dto.password)

    if (repo.findByEmail(email) != null) throw ConflictException("Email already in use")

    val hash = PasswordUtil.hash(dto.password)
    val user = repo.create(email, hash, dto.displayName)
    val token = issueTokenFor(user.id)
    return tokenResponse(token, user)
  }

  suspend fun login(dto: LoginDto): TokenResponse {
    val email = dto.email.trim().lowercase()
    val user = repo.findByEmail(email) ?: throw UnauthorizedException("Invalid credentials")
    if (!PasswordUtil.verify(dto.password, user.passwordHash)) throw UnauthorizedException("Invalid credentials")
    val token = issueTokenFor(user.id)
    return tokenResponse(token, user)
  }

  suspend fun getMe(principal: JWTPrincipal): MeDto {
    val userId = principal.subject?.toIntOrNull() ?: throw UnauthorizedException()
    val user = repo.findById(userId) ?: throw UnauthorizedException()
    return MeDto(user.id, user.email, user.displayName)
  }

  suspend fun logout(principal: JWTPrincipal) {
    val jti = principal.jwtId ?: return
    TokenStore.revokeJti(jti)
  }

  suspend fun logoutAll(principal: JWTPrincipal) {
    val userId = principal.subject?.toIntOrNull() ?: return
    TokenStore.revokeAllForUser(userId)
  }

  suspend fun deleteMe(principal: JWTPrincipal) {
    val userId = principal.subject?.toIntOrNull() ?: throw UnauthorizedException()
    repo.deleteById(userId)
    TokenStore.revokeAllForUser(userId)
  }

  suspend fun requestPasswordReset(emailRaw: String) {
    val email = emailRaw.trim().lowercase()
    val user = repo.findByEmail(email) ?: return // avoid leaking user existence
    val otp = otpService.generateOtp(email)
    emailService.sendPasswordResetOtp(user.email, otp)
  }

  suspend fun resetPassword(emailRaw: String, otp: String, newPassword: String): TokenResponse {
    val email = emailRaw.trim().lowercase()
    validatePassword(newPassword)

    val user = repo.findByEmail(email) ?: throw UnauthorizedException("Invalid OTP or email")
    if (!otpService.verifyOtp(email, otp)) throw UnauthorizedException("Invalid OTP or email")

    val newHash = PasswordUtil.hash(newPassword)
    repo.updatePassword(user.id, newHash)

    TokenStore.revokeAllForUser(user.id)

    // Create a fresh token; we can reuse the same (email/displayName) since only password changed
    val issued = issueTokenFor(user.id)
    return tokenResponse(issued, user)
  }

  // ---- helpers ----

  private fun issueTokenFor(userId: Int): IssuedToken {
    val issued = JwtConfig.generateToken(userId)
    TokenStore.registerIssuedToken(userId, issued.jti, issued.expiresAtMillis)
    return issued
  }

  private fun tokenResponse(issued: IssuedToken, user: UserRecord): TokenResponse {
    return TokenResponse(
      accessToken = issued.token,
      expiresIn = JwtConfig.validityMs / 1000L,
      user = UserDto(user.id, user.email, user.displayName)
    )
  }

  private fun validateEmail(email: String) {
    val regex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$".toRegex(RegexOption.IGNORE_CASE)
    if (!regex.matches(email)) throw ValidationException("Invalid email")
  }

  private fun validatePassword(password: String) {
    if (password.length !in 8..128) throw ValidationException("Password length must be 8-128 characters")
  }

  companion object {
    fun default(app: Application): AuthService {
      val repo: UserRepository = DefaultUserRepository
      val email = DevEmailService(app.environment.log)
      val otp = InMemoryOtpService(app.environment.log)
      return AuthService(repo, email, otp)
    }
  }
}
