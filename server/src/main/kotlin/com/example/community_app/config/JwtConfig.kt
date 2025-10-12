package com.example.community_app.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import java.util.*

object JwtConfig {
  lateinit var secret: String
  lateinit var issuer: String
  lateinit var audience: String
  var validityMs: Long = 0
  private lateinit var algorithm: Algorithm

  fun init(cfg: ApplicationConfig) {
    secret = cfg.property("ktor.jwt.secret").getString()
    issuer = cfg.property("ktor.jwt.issuer").getString()
    audience = cfg.property("ktor.jwt.audience").getString()
    validityMs = cfg.property("ktor.jwt.validityMs").getString().toLong()
    algorithm = Algorithm.HMAC256(secret)
  }

  fun generateToken(userId: Int, email: String): String = JWT.create()
    .withAudience(audience)
    .withIssuer(issuer)
    .withClaim("userId", userId)
    .withClaim("email", email)
    .withExpiresAt(Date(System.currentTimeMillis() + validityMs))
    .sign(algorithm)

  val verifier get() = JWT.require(algorithm).withIssuer(issuer).build()
}