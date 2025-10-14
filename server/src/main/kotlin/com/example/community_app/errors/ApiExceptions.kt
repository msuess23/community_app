package com.example.community_app.errors

import io.ktor.http.*

open class ApiException(val status: HttpStatusCode, val code: String, message: String): RuntimeException(message)
class ValidationException(message: String): ApiException(HttpStatusCode.UnprocessableEntity, "422", message)
class ConflictException(message: String): ApiException(HttpStatusCode.Conflict, "409", message)
class UnauthorizedException(message: String = "Unauthorized"): ApiException(HttpStatusCode.Unauthorized, "401", message)
class ForbiddenException(message: String = "Forbidden"): ApiException(HttpStatusCode.Forbidden, "403", message)
class NotFoundException(message: String): ApiException(HttpStatusCode.NotFound, "404", message)

class BadRequestException(message: String) : RuntimeException(message)
