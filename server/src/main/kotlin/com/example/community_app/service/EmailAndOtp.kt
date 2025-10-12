package com.example.community_app.service

import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface EmailService {
  fun sendPasswordResetOtp(email: String, otp: String)
}

class DevEmailService(private val logger: Logger): EmailService {
  override fun sendPasswordResetOtp(email: String, otp: String) {
    logger.info("[DEV EMAIL] Sending password reset OTP to {}: {}", email, otp)
  }
}

interface OtpService {
  fun generateOtp(email: String, ttl: Duration = 10.minutes): String
  fun verifyOtp(email: String, otp: String): Boolean
}

class InMemoryOtpService(private val logger: Logger): OtpService {
  private data class Entry(val code: String, val expiresAt: Long)
  private val store = ConcurrentHashMap<String, Entry>()

  override fun generateOtp(email: String, ttl: Duration): String {
    val code = (100000..999999).random().toString()
    store[email] = Entry(code, System.currentTimeMillis() + ttl.inWholeMilliseconds)
    logger.info("[DEV OTP] Generated OTP for {}: {} (valid {} ms)", email, code, ttl.inWholeMilliseconds)
    return code
  }

  override fun verifyOtp(email: String, otp: String): Boolean {
    val entry = store[email] ?: return false
    if (entry.expiresAt < System.currentTimeMillis()) {
      store.remove(email)
      return false
    }
    val ok = entry.code == otp
    if (ok) store.remove(email)
    return ok
  }
}
