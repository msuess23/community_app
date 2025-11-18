package com.example.community_app.util

import com.example.community_app.dto.AddressDto
import com.example.community_app.errors.ValidationException
import java.time.Instant
import java.time.format.DateTimeParseException

/** Parse ISO-8601 (UTC) mit konsistenter Fehlermeldung. */
fun parseInstantStrict(s: String): Instant =
  try { Instant.parse(s) } catch (e: DateTimeParseException) {
    throw ValidationException("Invalid datetime: $s (expected ISO-8601 UTC)")
  }

/** Parse "minLon,minLat,maxLon,maxLat" → DoubleArray[4]. */
fun parseBbox(s: String): DoubleArray {
  val parts = s.split(",").map { it.trim() }
  if (parts.size != 4) throw ValidationException("bbox must be 'minLon,minLat,maxLon,maxLat'")
  return DoubleArray(4) { i -> parts[i].toDouble() }
}

/** * Adresse/Geodaten prüfen.
 * Latitude/Longitude müssen valide sein.
 * Optionale Felder (Street etc.) könnten hier ebenfalls validiert werden (z.B. max Länge).
 */
fun validateAddress(addr: AddressDto?) {
  if (addr == null) return
  if (addr.longitude !in -180.0..180.0) throw ValidationException("longitude out of range")
  if (addr.latitude !in -90.0..90.0) throw ValidationException("latitude out of range")

  if (addr.street != null && addr.street.isBlank()) throw ValidationException("Street must not be blank if provided")
  // Weitere Checks optional...
}

/** Sicherstellen, dass e nach s liegt. */
fun ensureEndAfterStart(startsAt: Instant, endsAt: Instant) {
  if (!endsAt.isAfter(startsAt)) throw ValidationException("endsAt must be after startsAt")
}