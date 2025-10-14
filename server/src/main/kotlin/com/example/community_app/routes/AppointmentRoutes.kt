package com.example.community_app.routes

import com.example.community_app.dto.SlotBatchCreateDto
import com.example.community_app.repository.DefaultAppointmentRepository
import com.example.community_app.service.AppointmentService
import com.example.community_app.util.requireUserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Slots = freie Zeitfenster je Office
 * Appointments = gebuchte Termine je User
 */
fun Route.appointmentRoutes(
  service: AppointmentService = AppointmentService(DefaultAppointmentRepository)
) {
  // ----- Office-bezogene Slot-Verwaltung -----
  route("/offices/{officeId}/appointments") {

    get("/free") {
      val officeId = call.parameters["officeId"]!!.toInt()
      val from = call.request.queryParameters["from"]
      val to = call.request.queryParameters["to"]
      val items = service.listFreeSlots(officeId, from, to)
      call.respond(items)
    }

    authenticate("auth-jwt") {
      post("/slots") {
        val officeId = call.parameters["officeId"]!!.toInt()
        val body = call.receive<SlotBatchCreateDto>()
        val created = service.createSlots(officeId, body)
        call.respond(HttpStatusCode.Created, created)
      }

      delete("/slots/{slotId}") {
        val officeId = call.parameters["officeId"]!!.toInt()
        val slotId = call.parameters["slotId"]!!.toInt()
        service.deleteSlot(officeId, slotId)
        call.respond(HttpStatusCode.NoContent)
      }

      post("/{appointmentId}") {
        val officeId = call.parameters["officeId"]!!.toInt()
        val appointmentId = call.parameters["appointmentId"]!!.toInt()
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.requireUserId()
        val booked = service.bookById(officeId, appointmentId, userId)
        call.respond(booked)
      }
    }
  }

  // ----- User-bezogene Appointments -----
  authenticate("auth-jwt") {
    get("/appointments") {
      val principal = call.principal<JWTPrincipal>()!!
      val userId = principal.requireUserId()
      val items = service.listForUser(userId)
      call.respond(items)
    }

    get("/appointments/{appointmentId}") {
      val principal = call.principal<JWTPrincipal>()!!
      val userId = principal.requireUserId()
      val appointmentId = call.parameters["appointmentId"]!!.toInt()
      val dto = service.getForUser(appointmentId, userId)
      call.respond(dto)
    }

    delete("/appointments/{appointmentId}") {
      val principal = call.principal<JWTPrincipal>()!!
      val userId = principal.requireUserId()
      val appointmentId = call.parameters["appointmentId"]!!.toInt()
      val ok = service.cancel(appointmentId, userId)
      if (ok) call.respond(HttpStatusCode.NoContent)
      else call.respond(HttpStatusCode.NotFound)
    }
  }
}
