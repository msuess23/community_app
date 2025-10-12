package com.example.community_app.routes

import com.example.community_app.dto.LoginDto
import com.example.community_app.dto.RegisterDto
import com.example.community_app.service.AuthService
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
  val service = AuthService()

  route("/auth") {
    post("/register") {
      val dto = call.receive<RegisterDto>()
      val token = service.register(dto)
      call.respond(mapOf("accessToken" to token, "tokenType" to "Bearer"))
    }

    post("/login") {
      val dto = call.receive<LoginDto>()
      val token = service.login(dto)
      call.respond(mapOf("accessToken" to token, "tokenType" to "Bearer"))
    }

    authenticate("auth-jwt") {
      get("/me") {
        val principal = call.principal<JWTPrincipal>()
        call.respond(mapOf("userId" to principal?.payload?.getClaim("userId")?.asInt()))
      }
    }
  }
}
