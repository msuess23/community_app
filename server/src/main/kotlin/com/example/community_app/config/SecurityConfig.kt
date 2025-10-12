package com.example.community_app.config

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
  install(Authentication) {
    jwt("auth-jwt") {
      JwtConfig.configure(this)
    }
  }
}
