package com.example.community_app.errors

import kotlinx.serialization.Serializable

/**
 * Einheitliche Fehlerantwort für alle Endpunkte.
 * - status: numerischer HTTP-Status (z. B. 400)
 * - error: kurzer Status-Text (z. B. "Bad Request")
 * - code: optionales knapperes Fehlerkürzel fürs Frontend (z. B. "VALIDATION_ERROR")
 * - message: menschenlesbare Beschreibung
 * - path/method: Request-Kontext
 * - timestamp: ISO-8601 UTC, wann der Fehler generiert wurde
 * - details: optionale Feld-Fehler (z. B. Validierung)
 */
@Serializable
data class ErrorResponse(
  val status: Int,
  val error: String,
  val code: String? = null,
  val message: String? = null,
  val path: String? = null,
  val method: String? = null,
  val timestamp: String,
  val details: Map<String, String>? = null
)
