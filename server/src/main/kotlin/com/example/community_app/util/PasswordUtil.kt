package com.example.community_app.util

import org.bouncycastle.crypto.params.Argon2Parameters
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import java.security.SecureRandom
import java.util.*

object PasswordUtil {

  private const val SALT_LENGTH = 16
  private const val HASH_LENGTH = 32
  private const val ITERATIONS = 3
  private const val MEMORY_KB = 65536  // 64 MB
  private const val PARALLELISM = 1

  fun hash(password: String): String {
    val salt = ByteArray(SALT_LENGTH)
    SecureRandom().nextBytes(salt)

    val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
      .withSalt(salt)
      .withParallelism(PARALLELISM)
      .withMemoryAsKB(MEMORY_KB)
      .withIterations(ITERATIONS)
      .build()

    val generator = Argon2BytesGenerator()
    generator.init(params)
    val result = ByteArray(HASH_LENGTH)
    generator.generateBytes(password.toByteArray(Charsets.UTF_8), result, 0, result.size)

    return "${Base64.getEncoder().encodeToString(salt)}\$${Base64.getEncoder().encodeToString(result)}"
  }

  fun verify(password: String, stored: String): Boolean {
    val (saltB64, hashB64) = stored.split('$')
    val salt = Base64.getDecoder().decode(saltB64)
    val expected = Base64.getDecoder().decode(hashB64)

    val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
      .withSalt(salt)
      .withParallelism(PARALLELISM)
      .withMemoryAsKB(MEMORY_KB)
      .withIterations(ITERATIONS)
      .build()

    val generator = Argon2BytesGenerator()
    generator.init(params)
    val result = ByteArray(HASH_LENGTH)
    generator.generateBytes(password.toByteArray(Charsets.UTF_8), result, 0, result.size)

    return result.contentEquals(expected)
  }
}
