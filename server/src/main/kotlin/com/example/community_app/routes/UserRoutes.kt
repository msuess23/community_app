package com.example.community_app.routes

import com.example.community_app.dto.SettingsUpdateDto
import com.example.community_app.dto.UserUpdateDto
import com.example.community_app.service.SettingsService
import com.example.community_app.service.UserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(
  userService: UserService = UserService.default(),
  settingsService: SettingsService = SettingsService.default()
) {
  authenticate("auth-jwt") {
    route("/user/me") {
      // --- get user ---
      get {
        val principal = call.principal<JWTPrincipal>()!!
        val me = userService.getMe(principal)
        call.respond(me)
      }

      // --- update user ---
      put {
        val principal = call.principal<JWTPrincipal>()!!
        val dto = call.receive<UserUpdateDto>()
        val updated = userService.updateMe(principal, dto)
        call.respond(updated)
      }
    }

    route("/settings") {
      // --- get settings of user ---
      get {
        val principal = call.principal<JWTPrincipal>()!!
        val settings = settingsService.get(principal)
        if (settings == null) call.respond(HttpStatusCode.NoContent) else call.respond(settings)
      }

      // --- update settings of user ---
      put {
        val principal = call.principal<JWTPrincipal>()!!
        val patch = call.receive<SettingsUpdateDto>()
        val upserted = settingsService.put(principal, patch)
        call.respond(upserted)
      }

      // --- delete settings of user ---
      delete {
        val principal = call.principal<JWTPrincipal>()!!
        settingsService.delete(principal)
        call.respond(HttpStatusCode.NoContent)
      }
    }
  }
}
