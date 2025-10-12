package com.example.community_app.routes

import com.example.community_app.config.userId
import com.example.community_app.dto.*
import com.example.community_app.service.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
  val auth = AuthService()

  route("/auth") {
    post("/register") {
      val dto = call.receive<RegisterDto>()
      call.respond(HttpStatusCode.Created, auth.register(dto))
    }

    post("/login") {
      val dto = call.receive<LoginDto>()
      call.respond(auth.login(dto))
    }

    authenticate("auth-jwt") {
      get("/me") {
        call.respond(auth.me(call.userId()))
      }
      post("/change-password") {
        val dto = call.receive<ChangePasswordDto>()
        auth.changePassword(call.userId(), dto)
        call.respond(HttpStatusCode.NoContent)
      }
    }
  }
}
