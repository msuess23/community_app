package com.example.community_app.routes

import com.example.community_app.util.requireAdmin
import com.example.community_app.dto.OfficeCreateDto
import com.example.community_app.dto.OfficeUpdateDto
import com.example.community_app.service.OfficeService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.officeRoutes(
  service: OfficeService = OfficeService(com.example.community_app.repository.DefaultOfficeRepository)
) {
  route("/office") {
    // --- get all offices ---
    get {
      val q = call.request.queryParameters["q"]
      val bbox = call.request.queryParameters["bbox"]
      val list = service.list(q, bbox)
      call.respond(list)
    }

    // --- get specific office ---
    get("/{id}") {
      val id = call.parameters["id"]!!.toInt()
      val dto = service.get(id)
      call.respond(dto)
    }

    // Admin-only CRUD
    authenticate("auth-jwt") {
      // --- create new office ---
      post {
        val principal = call.principal<JWTPrincipal>()!!
        principal.requireAdmin()
        val dto = call.receive<OfficeCreateDto>()
        val created = service.create(dto)
        call.response.headers.append(HttpHeaders.Location, "/api/offices/${created.id}")
        call.respond(HttpStatusCode.Created, created)
      }

      // --- update office ---
      put("/{id}") {
        val principal = call.principal<JWTPrincipal>()!!
        principal.requireAdmin()
        val id = call.parameters["id"]!!.toInt()
        val patch = call.receive<OfficeUpdateDto>()
        val updated = service.update(id, patch)
        call.respond(updated)
      }

      // --- delete office ---
      delete("/{id}") {
        val principal = call.principal<JWTPrincipal>()!!
        principal.requireAdmin()
        val id = call.parameters["id"]!!.toInt()
        service.delete(id)
        call.respond(HttpStatusCode.NoContent)
      }
    }
  }
}
