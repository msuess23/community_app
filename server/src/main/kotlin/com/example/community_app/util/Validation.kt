package com.example.community_app.util

import com.example.community_app.dto.LocationDto
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

/** Longitude/Latitude-Range prüfen. Null = ok (nicht vorhanden). */
fun validateLocation(loc: LocationDto?) {
  if (loc == null) return
  if (loc.longitude !in -180.0..180.0) throw ValidationException("longitude out of range")
  if (loc.latitude !in -90.0..90.0) throw ValidationException("latitude out of range")
}

/** Sicherstellen, dass e nach s liegt. */
fun ensureEndAfterStart(startsAt: Instant, endsAt: Instant) {
  if (!endsAt.isAfter(startsAt)) throw ValidationException("endsAt must be after startsAt")
}
