package com.example.community_app.config

import com.example.community_app.errors.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

@kotlinx.serialization.Serializable
data class ErrorResponse(val code: String, val message: String)

fun Application.configureExceptionHandling() {
  install(StatusPages) {
    exception<ApiException> { call, e ->
      call.respond(e.status, ErrorResponse(e.code, e.message ?: e.status.description))
    }
    exception<IllegalArgumentException> { call, e ->
      call.respond(HttpStatusCode.BadRequest, ErrorResponse("400", e.message ?: "Bad Request"))
    }
    exception<Throwable> { call, cause ->
      call.application.log.error("Unhandled exception", cause)
      call.respond(HttpStatusCode.InternalServerError, ErrorResponse("500", "Internal Server Error"))
    }
  }
}
