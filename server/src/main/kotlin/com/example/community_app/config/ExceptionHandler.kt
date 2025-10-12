package com.example.community_app.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureExceptionHandling() {
  install(StatusPages) {
    exception<AuthenticationException> { call, _ ->
      call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
    }
    exception<IllegalArgumentException> { call, cause ->
      call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
    }
    exception<Throwable> { call, cause ->
      call.application.log.error("Unhandled exception", cause)
      call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
    }
  }
}
