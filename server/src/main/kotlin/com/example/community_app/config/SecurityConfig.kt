package com.example.community_app.config

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
  install(Authentication) {
    jwt("auth-jwt") {
      realm = JwtConfig.realm
      verifier(JwtConfig.verifier)
      validate { credential ->
        val userId = credential.payload.getClaim("userId").asInt()
        if (userId != null) JWTPrincipal(credential.payload) else null
      }
    }
  }
}

// Helper für Routen
fun io.ktor.server.application.ApplicationCall.userId(): Int =
  principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asInt()
    ?: throw AuthenticationException()

class AuthenticationException : RuntimeException()
