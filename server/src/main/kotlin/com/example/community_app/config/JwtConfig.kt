package com.example.community_app.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.config.*
import java.util.*

object JwtConfig {
  private lateinit var algorithm: Algorithm
  lateinit var secret: String
  lateinit var issuer: String
  lateinit var audience: String
  lateinit var realm: String
  var validityMs: Long = 0

  fun init(config: ApplicationConfig) {
    val jwtConfig = config.config("ktor.jwt")
    secret = jwtConfig.property("secret").getString()
    issuer = jwtConfig.property("issuer").getString()
    audience = jwtConfig.property("audience").getString()
    realm = jwtConfig.property("realm").getString()
    validityMs = jwtConfig.property("validityMs").getString().toLong()
    algorithm = Algorithm.HMAC256(secret)
  }

  fun generateToken(userId: Int, email: String): String = JWT.create()
    .withAudience(audience)
    .withIssuer(issuer)
    .withClaim("userId", userId)
    .withClaim("email", email)
    .withExpiresAt(Date(System.currentTimeMillis() + validityMs))
    .sign(algorithm)

  val verifier: JWTVerifier get() = JWT.require(algorithm).withIssuer(issuer).withAudience(audience).build()
}