package com.example.community_app.routes

import com.example.community_app.util.TicketCategory
import com.example.community_app.dto.*
import com.example.community_app.service.TicketService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.ticketRoutes(
  service: TicketService = TicketService.default()
) {
  route("/tickets") {

    // Public list (only PUBLIC visibility)
    get {
      val officeId = call.request.queryParameters["officeId"]?.toIntOrNull()
      val category = call.request.queryParameters["category"]?.let { runCatching { TicketCategory.valueOf(it) }.getOrNull() }
      val createdFrom = call.request.queryParameters["createdFrom"]
      val createdTo = call.request.queryParameters["createdTo"]
      val bbox = call.request.queryParameters["bbox"]
      val principal = call.principal<JWTPrincipal>() // optional (für userVoted)
      val list = service.listPublic(officeId, category, createdFrom, createdTo, bbox, principal)
      call.respond(list)
    }

    // Public detail (PRIVATE nur sichtbar wenn berechtigt)
    get("/{id}") {
      val id = call.parameters["id"]!!.toInt()
      val principal = call.principal<JWTPrincipal>()
      val dto = service.getPublicAware(id, principal)
      call.respond(dto)
    }

    // Public status endpoints (Zugriff wird im Service geprüft)
    get("/{id}/status") {
      val id = call.parameters["id"]!!.toInt()
      val principal = call.principal<JWTPrincipal>()
      val items = service.listStatus(id, principal)
      call.respond(items)
    }
    get("/{id}/status/current") {
      val id = call.parameters["id"]!!.toInt()
      val principal = call.principal<JWTPrincipal>()
      val current = service.currentStatus(id, principal)
      if (current == null) call.respond(HttpStatusCode.NoContent) else call.respond(current)
    }

    // Authenticated
    authenticate("auth-jwt") {

      post {
        val principal = call.principal<JWTPrincipal>()!!
        val body = call.receive<TicketCreateDto>()
        val created = service.create(principal, body)
        call.response.headers.append(HttpHeaders.Location, "/api/tickets/${created.id}")
        call.respond(HttpStatusCode.Created, created)
      }

      put("/{id}") {
        val principal = call.principal<JWTPrincipal>()!!
        val id = call.parameters["id"]!!.toInt()
        val patch = call.receive<TicketUpdateDto>()
        val updated = service.update(principal, id, patch)
        call.respond(updated)
      }

      delete("/{id}") {
        val principal = call.principal<JWTPrincipal>()!!
        val id = call.parameters["id"]!!.toInt()
        service.delete(principal, id)
        call.respond(HttpStatusCode.NoContent)
      }

      // Status setzen (Officer/Admin)
      put("/{id}/status") {
        val principal = call.principal<JWTPrincipal>()!!
        val id = call.parameters["id"]!!.toInt()
        val body = call.receive<TicketStatusCreateDto>()
        val upd = service.addStatus(principal, id, body)
        call.respond(upd)
      }

      // Voting
      post("/{id}/vote") {
        val principal = call.principal<JWTPrincipal>()!!
        val id = call.parameters["id"]!!.toInt()
        val res = service.vote(principal, id)
        call.respond(res)
      }

      delete("/{id}/vote") {
        val principal = call.principal<JWTPrincipal>()!!
        val id = call.parameters["id"]!!.toInt()
        val res = service.unvote(principal, id)
        call.respond(res)
      }
    }

    // Votes summary (öffentlich, aber private nur wenn berechtigt)
    get("/{id}/votes") {
      val id = call.parameters["id"]!!.toInt()
      val principal = call.principal<JWTPrincipal>()
      val res = service.votesSummary(id, principal)
      call.respond(res)
    }
  }
}
