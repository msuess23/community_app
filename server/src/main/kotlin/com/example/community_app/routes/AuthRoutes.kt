package com.example.community_app.routes

import com.example.community_app.dto.*
import com.example.community_app.service.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun Route.authRoutes(authService: AuthService? = null) {
  val service = authService ?: this.application.getAuthService()

  route("/auth") {
    post("/register") {
      val dto = call.receive<RegisterDto>()
      val response = service.register(dto)
      call.response.headers.append(HttpHeaders.Location, "/api/auth/me")
      call.respond(HttpStatusCode.Created, response)
    }
    post("/login") {
      val dto = call.receive<LoginDto>()
      val response = service.login(dto)
      call.respond(HttpStatusCode.OK, response)
    }
    post("/forgot-password") {
      val req = call.receive<ForgotPasswordRequest>()
      service.requestPasswordReset(req.email)
      call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
    }
    post("/reset-password") {
      val req = call.receive<ResetPasswordRequest>()
      val response = service.resetPassword(req.email, req.otp, req.newPassword)
      call.respond(HttpStatusCode.OK, response)
    }
    authenticate("auth-jwt") {
      get("/me") {
        val principal = call.principal<JWTPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
        val user = service.getMe(principal)
        call.respond(user)
      }
      post("/logout") {
        val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
        service.logout(principal)
        call.respond(HttpStatusCode.NoContent)
      }
      post("/logout-all") {
        val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
        service.logoutAll(principal)
        call.respond(HttpStatusCode.NoContent)
      }
      delete("/delete") {
        val principal = call.principal<JWTPrincipal>() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
        service.deleteMe(principal)
        call.respond(HttpStatusCode.NoContent)
      }
    }
  }
}

private val AUTH_SERVICE_KEY = AttributeKey<AuthService>("AuthService")
private fun Application.getAuthService(): AuthService {
  return if (attributes.contains(AUTH_SERVICE_KEY)) {
    attributes[AUTH_SERVICE_KEY]
  } else {
    val svc = AuthService.default(this)
    attributes.put(AUTH_SERVICE_KEY, svc)
    svc
  }
}
