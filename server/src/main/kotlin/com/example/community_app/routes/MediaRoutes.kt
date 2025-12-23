package com.example.community_app.routes

import com.example.community_app.dto.MediaDto
import com.example.community_app.errors.ForbiddenException
import com.example.community_app.util.MediaTargetType
import com.example.community_app.repository.DefaultMediaRepository
import com.example.community_app.repository.DefaultTicketRepository
import com.example.community_app.service.MediaService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Generische Medienrouten:
 *   - /media/{type}/{targetId}            (list)
 *   - /media/{type}/{targetId} POST       (upload)
 *   - /media/{type}/{targetId}/{mediaId}  (delete)
 *   - /media/{mediaId}                    (binary serving)
 *
 * Zusätzlich: Kompatible Ticket-Routen (/tickets/{ticketId}/media ...)
 */
fun Route.mediaRoutes(
  service: MediaService = MediaService(DefaultMediaRepository, DefaultTicketRepository)
) {

  // ---------- Generic target-based media ----------

  route("/media") {

    // Public/Authed list (Policy im Service)
    authenticate("auth-jwt", optional = true) {
      get("/{type}/{targetId}") {
        val type = call.parameters["type"]!!.uppercase()
        val targetId = call.parameters["targetId"]!!.toInt()
        val principal = call.principal<JWTPrincipal>()
        val items = service.list(MediaTargetType.valueOf(type), targetId, principal)
        call.respond(items)
      }
    }

    authenticate("auth-jwt") {
      // Upload (multipart/form-data, Feld: file)
      post("/{type}/{targetId}") {
        val type = call.parameters["type"]!!.uppercase()
        val targetId = call.parameters["targetId"]!!.toInt()
        val principal = call.principal<JWTPrincipal>()!!
        val multipart = call.receiveMultipart()

        var created = false
        var dto: MediaDto? = null

        multipart.forEachPart { part ->
          when (part) {
            is PartData.FileItem -> {
              try {
                if (!created) {
                  dto = service.upload(MediaTargetType.valueOf(type), targetId, principal, part)
                  created = true
                }
              } catch (e: ForbiddenException) {
                part.streamProvider().use { it.readBytes() }
                part.dispose()
                throw e
              }
              part.dispose()
            }
            else -> part.dispose()
          }
        }

        if (!created || dto == null) {
          call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No file part 'file' found"))
          return@post
        }

        call.response.headers.append(HttpHeaders.Location, dto.url)
        call.respond(HttpStatusCode.Created, dto)
      }

      delete("/{type}/{targetId}/{mediaId}") {
        val type = call.parameters["type"]!!.uppercase()
        val targetId = call.parameters["targetId"]!!.toInt()
        val mediaId = call.parameters["mediaId"]!!.toInt()
        val principal = call.principal<JWTPrincipal>()!!
        service.delete(MediaTargetType.valueOf(type), targetId, mediaId, principal)
        call.respond(HttpStatusCode.NoContent)
      }
    }
  }

  authenticate("auth-jwt") {
    put("/media/{mediaId}/cover") {
      val mediaId = call.parameters["mediaId"]!!.toInt()
      val principal = call.principal<JWTPrincipal>()!!
      val updated = service.setCover(mediaId, principal)
      call.respond(updated)
    }
  }

  // Eigenständige Binary-Route (damit es keine Kollision mit /media/{type}/... gibt)
  authenticate("auth-jwt", optional = true) {
    get("/media/{mediaId}") {
      val mediaId = call.parameters["mediaId"]!!.toInt()
      val principal = call.principal<JWTPrincipal>() // Jetzt korrekt befüllt
      val bin = service.getBinary(mediaId, principal)

      call.response.header(HttpHeaders.ContentType, bin.mimeType)
      call.response.header(
        HttpHeaders.ContentDisposition,
        ContentDisposition.Inline.withParameter(
          ContentDisposition.Parameters.FileName,
          bin.filename
        ).toString()
      )
      call.respondFile(bin.file)
    }
  }

  // ---------- Compatibility Ticket aliases ----------
  // Beibehalten für bestehende Clients; intern generisches Service.

  route("/ticket/{ticketId}/media") {
    // list
    authenticate("auth-jwt", optional = true) {
      get {
        val ticketId = call.parameters["ticketId"]!!.toInt()
        val principal = call.principal<JWTPrincipal>()
        val items = service.list(MediaTargetType.TICKET, ticketId, principal)
        call.respond(items)
      }
    }
    // upload/delete
    authenticate("auth-jwt") {
      post {
        val ticketId = call.parameters["ticketId"]!!.toInt()
        val principal = call.principal<JWTPrincipal>()!!
        val multipart = call.receiveMultipart()
        var created = false
        var dto: MediaDto? = null
        multipart.forEachPart { part ->
          when (part) {
            is PartData.FileItem -> {
              if (!created) {
                dto = service.upload(MediaTargetType.TICKET, ticketId, principal, part)
                created = true
              }
              part.dispose()
            }
            else -> part.dispose()
          }
        }
        if (!created || dto == null) {
          call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No file part 'file' found"))
          return@post
        }
        call.response.headers.append(HttpHeaders.Location, dto.url)
        call.respond(HttpStatusCode.Created, dto)
      }
      delete("/{mediaId}") {
        val ticketId = call.parameters["ticketId"]!!.toInt()
        val mediaId = call.parameters["mediaId"]!!.toInt()
        val principal = call.principal<JWTPrincipal>()!!
        service.delete(MediaTargetType.TICKET, ticketId, mediaId, principal)
        call.respond(HttpStatusCode.NoContent)
      }
    }
  }
}
