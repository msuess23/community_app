package com.example.community_app.util

import com.example.community_app.errors.ForbiddenException
import com.example.community_app.errors.UnauthorizedException
import io.ktor.server.auth.jwt.*

fun JWTPrincipal.requireUserId(): Int =
  this.subject?.toIntOrNull() ?: throw UnauthorizedException()

fun JWTPrincipal.requireRoleAtLeast(required: Role) {
  val actual = this.payload.getClaim("role").asString()?.let { Role.valueOf(it) } ?: Role.CITIZEN
  if (actual.ordinal < required.ordinal) throw ForbiddenException()
}

fun JWTPrincipal.requireAdmin() = requireRoleAtLeast(Role.ADMIN)

fun JWTPrincipal.requireOfficerOf(officeId: Int) {
  val role = this.payload.getClaim("role").asString()?.let { Role.valueOf(it) } ?: Role.CITIZEN
  if (role == Role.ADMIN) return
  val claimOfficeId = this.payload.getClaim("officeId").asInt() // null wenn kein Claim
  if (role != Role.OFFICER || claimOfficeId == null || claimOfficeId != officeId) {
    throw ForbiddenException()
  }
}
