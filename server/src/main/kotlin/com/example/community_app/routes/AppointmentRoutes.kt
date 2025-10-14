package com.example.community_app.routes

import com.example.community_app.util.requireOfficerOf
import com.example.community_app.util.requireUserId
import com.example.community_app.dto.AppointmentCreateDto
import com.example.community_app.dto.SlotBatchCreateDto
import com.example.community_app.service.AppointmentService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.appointmentRoutes(
  service: AppointmentService = AppointmentService(com.example.community_app.repository.DefaultAppointmentRepository)
) {
  // Public: list free slots for an office
  get("/offices/{officeId}/slots") {
    val officeId = call.parameters["officeId"]!!.toInt()
    val from = call.request.queryParameters["from"]
    val to = call.request.queryParameters["to"]
    val slots = service.listFreeSlots(officeId, from, to)
    call.respond(slots)
  }

  authenticate("auth-jwt") {
    // Officer/Admin: create batch of free slots
    post("/offices/{officeId}/slots") {
      val principal = call.principal<JWTPrincipal>()!!
      val officeId = call.parameters["officeId"]!!.toInt()
      principal.requireOfficerOf(officeId) // admin passes too

      val batch = call.receive<SlotBatchCreateDto>()
      val created = service.createSlots(officeId, batch)
      call.respond(HttpStatusCode.Created, created)
    }

    // Officer/Admin: delete a free slot (not booked)
    delete("/offices/{officeId}/slots/{slotId}") {
      val principal = call.principal<JWTPrincipal>()!!
      val officeId = call.parameters["officeId"]!!.toInt()
      principal.requireOfficerOf(officeId)

      val slotId = call.parameters["slotId"]!!.toInt()
      val ok = service.deleteSlot(officeId, slotId)
      if (ok) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }

    // Citizen: book a slot
    post("/offices/{officeId}/appointments") {
      val principal = call.principal<JWTPrincipal>()!!
      val userId = principal.requireUserId()
      val officeId = call.parameters["officeId"]!!.toInt()
      val dto = call.receive<AppointmentCreateDto>()
      val appt = service.book(officeId, dto, userId)
      call.respond(HttpStatusCode.Created, appt)
    }

    // Citizen: list own appointments
    get("/users/me/appointments") {
      val principal = call.principal<JWTPrincipal>()!!
      val userId = principal.requireUserId()
      val list = service.listForUser(userId)
      call.respond(list)
    }

    // Citizen: cancel own appointment
    delete("/appointments/{id}") {
      val principal = call.principal<JWTPrincipal>()!!
      val userId = principal.requireUserId()
      val id = call.parameters["id"]!!.toInt()
      val ok = service.cancel(id, userId)
      if (ok) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
  }
}
