package com.example.community_app.util

import com.example.community_app.errors.ForbiddenException
import com.example.community_app.errors.UnauthorizedException
import io.ktor.server.auth.jwt.*

/** ---- JWT helpers ---- */
fun JWTPrincipal.requireUserId(): Int =
  this.subject?.toIntOrNull() ?: throw UnauthorizedException()

fun JWTPrincipal.userIdOrNull(): Int? =
  this.subject?.toIntOrNull()

fun JWTPrincipal.role(): Role =
  this.payload.getClaim("role").asString()
    ?.let { runCatching { Role.valueOf(it) }.getOrNull() }
    ?: Role.CITIZEN

fun JWTPrincipal.officeIdClaim(): Int? =
  this.payload.getClaim("officeId").asInt()

/** ---- Role checks ---- */
fun JWTPrincipal.requireRoleAtLeast(required: Role) {
  val actual = role()
  if (actual.ordinal < required.ordinal) throw ForbiddenException()
}

fun JWTPrincipal.requireAdmin() {
  if (role() != Role.ADMIN) throw ForbiddenException()
}

/** Officer for specific office (admins always pass). */
fun JWTPrincipal.requireOfficerOf(officeId: Int) {
  val r = role()
  if (r == Role.ADMIN) return
  val claimOfficeId = officeIdClaim()
  if (r != Role.OFFICER || claimOfficeId == null || claimOfficeId != officeId) {
    throw ForbiddenException()
  }
}

/** Officer of given office OR admin (officeId must be non-null for officers). */
fun requireOfficerOfOrAdmin(principal: JWTPrincipal, officeId: Int?) {
  val r = principal.role()
  if (r == Role.ADMIN) return
  val claim = principal.officeIdClaim()
  if (r != Role.OFFICER || officeId == null || claim == null || claim != officeId) {
    throw ForbiddenException()
  }
}

/** Creator OR officer of office OR admin can modify a resource. */
fun requireEditByCreatorOfficerOrAdmin(
  principal: JWTPrincipal,
  creatorUserId: Int,
  officeId: Int?
) {
  val uid = principal.requireUserId()
  val r = principal.role()
  val claim = principal.officeIdClaim()
  val isCreator = uid == creatorUserId
  val isAdmin = r == Role.ADMIN
  val isOfficerOf = (r == Role.OFFICER && claim != null && claim == officeId)
  if (!isCreator && !isAdmin && !isOfficerOf) {
    throw ForbiddenException("Not allowed to modify this resource")
  }
}

/** For PRIVATE visibility, only creator/admin/officer-of-office may view. */
fun ensureViewAllowedForVisibility(
  visibility: TicketVisibility,
  creatorUserId: Int,
  officeId: Int?,
  principal: JWTPrincipal?
) {
  if (visibility == TicketVisibility.PUBLIC) return
  // PRIVATE: principal must be present and allowed
  val role = principal?.role()
  val uid = principal?.userIdOrNull()
  val claimOfficeId = principal?.officeIdClaim()
  val isCreator = uid != null && uid == creatorUserId
  val isAdmin = role == Role.ADMIN
  val isOfficerOf = (role == Role.OFFICER && claimOfficeId != null && claimOfficeId == officeId)
  if (!isCreator && !isAdmin && !isOfficerOf) {
    throw ForbiddenException("Not allowed to view this resource")
  }
}
