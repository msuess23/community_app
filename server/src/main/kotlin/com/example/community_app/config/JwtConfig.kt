package com.example.community_app.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.community_app.util.TokenStore
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import java.util.*

data class IssuedToken(val token: String, val jti: String, val expiresAtMillis: Long)

object JwtConfig {
  private lateinit var algorithm: Algorithm
  private lateinit var secret: String
  lateinit var issuer: String
  lateinit var audience: String
  lateinit var realm: String
  var validityMs: Long = 0

  fun init(config: ApplicationConfig) {
    // Prefer environment variable for secret; fall back to config for dev.
    secret = System.getenv("JWT_SECRET") ?: config.property("ktor.jwt.secret").getString()
    issuer = config.property("ktor.jwt.issuer").getString()
    audience = config.property("ktor.jwt.audience").getString()
    realm = config.property("ktor.jwt.realm").getString()
    validityMs = config.property("ktor.jwt.validityMs").getString().toLong()

    algorithm = Algorithm.HMAC256(secret)
  }

  fun generateToken(userId: Int): IssuedToken {
    val now = System.currentTimeMillis()
    val exp = now + validityMs
    val jti = UUID.randomUUID().toString()

    val token = JWT.create()
      .withSubject(userId.toString())
      .withJWTId(jti)
      .withAudience(audience)
      .withIssuer(issuer)
      .withIssuedAt(Date(now))
      .withExpiresAt(Date(exp))
      .sign(algorithm)

    return IssuedToken(token = token, jti = jti, expiresAtMillis = exp)
  }

  fun configure(config: JWTAuthenticationProvider.Config) {
    val verifier = JWT
      .require(algorithm)
      .withIssuer(issuer)
      .withAudience(audience)
      .build()

    config.realm = realm
    config.verifier(verifier)
    config.validate { credential ->
      val sub = credential.payload.subject
      val jti = credential.jwtId
      if (sub.isNullOrBlank() || jti.isNullOrBlank()) return@validate null
      // deny if revoked
      if (TokenStore.isRevoked(jti)) return@validate null
      JWTPrincipal(credential.payload)
    }
  }
}
