package com.example.community_app.util

import java.util.concurrent.ConcurrentHashMap

/**
 * Simple in-memory token store for dev environments.
 * Tracks issued JTIs to support logout and logout-all.
 */
object TokenStore {
  // jti -> expiresAt
  private val revoked = ConcurrentHashMap<String, Long>()
  // userId -> set of active jti
  private val activeByUser = ConcurrentHashMap<Int, MutableSet<String>>()

  fun registerIssuedToken(userId: Int, jti: String, expiresAtMillis: Long) {
    cleanup()
    activeByUser.computeIfAbsent(userId) { mutableSetOf() }.add(jti)
  }

  fun revokeJti(jti: String) {
    revoked[jti] = System.currentTimeMillis() + 31536000000 // keep marker up to 1y
    // Also remove from user sets
    activeByUser.forEach { (_, set) -> set.remove(jti) }
  }

  fun revokeAllForUser(userId: Int) {
    activeByUser.remove(userId)?.forEach { jti -> revokeJti(jti) }
  }

  fun isRevoked(jti: String): Boolean {
    cleanup()
    val exp = revoked[jti] ?: return false
    if (exp < System.currentTimeMillis()) {
      revoked.remove(jti)
      return false
    }
    return true
  }

  private fun cleanup() {
    val now = System.currentTimeMillis()
    revoked.entries.removeIf { it.value < now }
    activeByUser.entries.removeIf { it.value.isEmpty() }
  }
}
