package com.example.community_app.config

import com.example.community_app.errors.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory
import java.time.Instant

private fun ApplicationCall.toErrorResponse(
  status: HttpStatusCode,
  code: String? = null,
  message: String? = null,
  details: Map<String, String>? = null
) = ErrorResponse(
  status = status.value,
  error = status.description,
  code = code,
  message = message,
  path = request.path(),
  method = request.httpMethod.value,
  timestamp = Instant.now().toString(),
  details = details
)

fun Application.configureExceptionHandling() {
  val log = LoggerFactory.getLogger("ExceptionHandler")

  install(StatusPages) {

    // 400er
    exception<BadRequestException> { call, cause ->
      call.respond(HttpStatusCode.BadRequest, call.toErrorResponse(
        status = HttpStatusCode.BadRequest,
        code = "BAD_REQUEST",
        message = cause.message
      ))
    }
    exception<ValidationException> { call, cause ->
      call.respond(HttpStatusCode.BadRequest, call.toErrorResponse(
        status = HttpStatusCode.BadRequest,
        code = "VALIDATION_ERROR",
        message = cause.message
      ))
    }
    // JSON/Body Fehler
    exception<SerializationException> { call, cause ->
      call.respond(HttpStatusCode.BadRequest, call.toErrorResponse(
        status = HttpStatusCode.BadRequest,
        code = "INVALID_BODY",
        message = cause.message ?: "Invalid request body"
      ))
    }
    exception<ContentTransformationException> { call, cause ->
      call.respond(HttpStatusCode.BadRequest, call.toErrorResponse(
        status = HttpStatusCode.BadRequest,
        code = "INVALID_BODY",
        message = cause.message ?: "Invalid request body"
      ))
    }

    // 401/403
    exception<UnauthorizedException> { call, cause ->
      call.respond(HttpStatusCode.Unauthorized, call.toErrorResponse(
        status = HttpStatusCode.Unauthorized,
        code = "UNAUTHORIZED",
        message = cause.message ?: "Authentication required"
      ))
    }
    exception<ForbiddenException> { call, cause ->
      call.respond(HttpStatusCode.Forbidden, call.toErrorResponse(
        status = HttpStatusCode.Forbidden,
        code = "FORBIDDEN",
        message = cause.message ?: "Not allowed"
      ))
    }

    // 404
    exception<NotFoundException> { call, cause ->
      call.respond(HttpStatusCode.NotFound, call.toErrorResponse(
        status = HttpStatusCode.NotFound,
        code = "NOT_FOUND",
        message = cause.message ?: "Resource not found"
      ))
    }

    // 409
    exception<ConflictException> { call, cause ->
      call.respond(HttpStatusCode.Conflict, call.toErrorResponse(
        status = HttpStatusCode.Conflict,
        code = "CONFLICT",
        message = cause.message
      ))
    }

    // Fallback 500
    exception<Throwable> { call, cause ->
      log.error("Unhandled exception at ${call.request.httpMethod.value} ${call.request.path()}", cause)
      call.respond(HttpStatusCode.InternalServerError, call.toErrorResponse(
        status = HttpStatusCode.InternalServerError,
        code = "INTERNAL_ERROR",
        message = "Unexpected server error"
      ))
    }
  }
}
