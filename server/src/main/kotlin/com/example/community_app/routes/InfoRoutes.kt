package com.example.community_app.routes

import com.example.community_app.util.InfoCategory
import com.example.community_app.dto.*
import com.example.community_app.service.InfoService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.infoRoutes(
  service: InfoService = InfoService.default()
) {
  route("/infos") {
    // --- get all infos ---
    get {
      val officeId = call.request.queryParameters["officeId"]?.toIntOrNull()
      val category = call.request.queryParameters["category"]?.let { runCatching { InfoCategory.valueOf(it) }.getOrNull() }
      val startsFrom = call.request.queryParameters["startsFrom"]
      val endsTo = call.request.queryParameters["endsTo"]
      val bbox = call.request.queryParameters["bbox"]
      val list = service.list(officeId, category, startsFrom, endsTo, bbox)
      call.respond(list)
    }

    // --- get specific info
    get("/{id}") {
      val id = call.parameters["id"]!!.toInt()
      val dto = service.get(id)
      call.respond(dto)
    }

    // --- get status history of info ---
    get("/{id}/status") {
      val id = call.parameters["id"]!!.toInt()
      val items = service.listStatus(id)
      call.respond(items)
    }

    // --- get current status of info ---
    get("/{id}/status/current") {
      val id = call.parameters["id"]!!.toInt()
      val current = service.getCurrentStatus(id)
      if (current == null) call.respond(HttpStatusCode.NoContent) else call.respond(current)
    }

    // Officer/Admin write
    authenticate("auth-jwt") {
      // --- create new info ---
      post {
        val principal = call.principal<JWTPrincipal>()!!
        val body = call.receive<InfoCreateDto>()
        val created = service.create(principal, body)
        call.response.headers.append(HttpHeaders.Location, "/api/infos/${created.id}")
        call.respond(HttpStatusCode.Created, created)
      }

      // --- update new info ---
      put("/{id}") {
        val principal = call.principal<JWTPrincipal>()!!
        val id = call.parameters["id"]!!.toInt()
        val patch = call.receive<InfoUpdateDto>()
        val updated = service.update(principal, id, patch)
        call.respond(updated)
      }

      // --- delete info ---
      delete("/{id}") {
        val principal = call.principal<JWTPrincipal>()!!
        val id = call.parameters["id"]!!.toInt()
        service.delete(principal, id)
        call.respond(HttpStatusCode.NoContent)
      }

      // --- update status of info ---
      put("/{id}/status") {
        val principal = call.principal<JWTPrincipal>()!!
        val id = call.parameters["id"]!!.toInt()
        val body = call.receive<StatusCreateDto>()
        val upd = service.addStatus(principal, id, body)
        call.respond(upd)
      }
    }
  }
}
