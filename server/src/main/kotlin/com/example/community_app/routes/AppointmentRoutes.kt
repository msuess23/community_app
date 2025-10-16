package com.example.community_app.routes

import com.example.community_app.dto.SlotBatchCreateDto
import com.example.community_app.repository.DefaultAppointmentRepository
import com.example.community_app.service.AppointmentService
import com.example.community_app.util.requireUserId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.appointmentRoutes(
  service: AppointmentService = AppointmentService(DefaultAppointmentRepository)
) {
  // --- free slots ---
  route("/offices/{officeId}/slots") {
    // --- get all free slots of an office (all) ---
    get {
      val officeId = call.parameters["officeId"]!!.toInt()
      val from = call.request.queryParameters["from"]
      val to = call.request.queryParameters["to"]
      val items = service.listFreeSlots(officeId, from, to)
      call.respond(items)
    }

    authenticate("auth-jwt") {
      // --- create free slots for office (officer/admin) ---
      post {
        val officeId = call.parameters["officeId"]!!.toInt()
        val body = call.receive<SlotBatchCreateDto>()
        val created = service.createSlots(officeId, body)
        call.respond(HttpStatusCode.Created, created)
      }

      // --- delete free slots for office (officer/admin) ---
      delete("/{slotId}") {
        val officeId = call.parameters["officeId"]!!.toInt()
        val slotId = call.parameters["slotId"]!!.toInt()
        service.deleteSlot(officeId, slotId)
        call.respond(HttpStatusCode.NoContent)
      }

      // --- book free slot as appointment (citizen) ---
      post("/{slotId}") {
        val officeId = call.parameters["officeId"]!!.toInt()
        val appointmentId = call.parameters["slotId"]!!.toInt()
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.requireUserId()
        val booked = service.bookById(officeId, appointmentId, userId)
        call.respond(booked)
      }
    }
  }

  route("/appointments") {
    authenticate("auth-jwt") {
      // --- get all appointments booked by user (citizen) ---
      get {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.requireUserId()
        val items = service.listForUser(userId)
        call.respond(items)
      }

      // --- get specific appointment (citizen) ---
      get("/{appointmentId}") {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.requireUserId()
        val appointmentId = call.parameters["appointmentId"]!!.toInt()
        val dto = service.getForUser(appointmentId, userId)
        call.respond(dto)
      }

      // --- cancel appointment (citizen) ---
      delete("/{appointmentId}") {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.requireUserId()
        val appointmentId = call.parameters["appointmentId"]!!.toInt()
        val ok = service.cancel(appointmentId, userId)
        if (ok) call.respond(HttpStatusCode.NoContent)
        else call.respond(HttpStatusCode.NotFound)
      }
    }
  }
}
