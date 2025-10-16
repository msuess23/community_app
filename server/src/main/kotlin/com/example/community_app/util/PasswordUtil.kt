package com.example.community_app.util

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

/**
 * Argon2id hashing with PHC string output:
 * $argon2id$v=19$m=65536,t=3,p=1$<salt_b64>$<hash_b64>
 */
object PasswordUtil {

  private const val SALT_LENGTH = 16
  private const val HASH_LENGTH = 32
  private const val ITERATIONS = 3
  private const val MEMORY_KB = 65536  // 64 MB
  private const val PARALLELISM = 1

  fun hash(password: String): String {
    val salt = ByteArray(SALT_LENGTH)
    SecureRandom().nextBytes(salt)

    val params = Argon2Parameters
      .Builder(Argon2Parameters.ARGON2_id) // Argon2id
      .withSalt(salt)
      .withParallelism(PARALLELISM)
      .withMemoryAsKB(MEMORY_KB)
      .withIterations(ITERATIONS)
      .build()

    val generator = Argon2BytesGenerator()
    generator.init(params)
    val result = ByteArray(HASH_LENGTH)
    generator.generateBytes(password.toByteArray(Charsets.UTF_8), result, 0, result.size)

    val saltB64 = Base64.getEncoder().withoutPadding().encodeToString(salt)
    val hashB64 = Base64.getEncoder().withoutPadding().encodeToString(result)

    return "\$argon2id\$v=19\$m=$MEMORY_KB,t=$ITERATIONS,p=$PARALLELISM\$$saltB64\$$hashB64"
  }

  fun verify(password: String, phc: String): Boolean {
    // Expected format: $argon2id$v=19$m=...,t=...,p=...$<salt>$<hash>
    val parts = phc.split('$')
    if (parts.size < 6 || parts[1] != "argon2id") return false

    val paramsPart = parts[3]
    val salt = Base64.getDecoder().decode(parts[4])
    val expectedHash = Base64.getDecoder().decode(parts[5])

    val (mem, iters, par) = parseParams(paramsPart)

    val params = Argon2Parameters
      .Builder(Argon2Parameters.ARGON2_id)
      .withSalt(salt)
      .withParallelism(par)
      .withMemoryAsKB(mem)
      .withIterations(iters)
      .build()

    val generator = Argon2BytesGenerator()
    generator.init(params)
    val candidate = ByteArray(expectedHash.size)
    generator.generateBytes(password.toByteArray(Charsets.UTF_8), candidate, 0, candidate.size)

    return MessageDigest.isEqual(candidate, expectedHash)
  }

  private fun parseParams(s: String): Triple<Int, Int, Int> {
    var m = MEMORY_KB
    var t = ITERATIONS
    var p = PARALLELISM
    s.split(',').forEach { kv ->
      val (k, v) = kv.split('=')
      when (k) {
        "m" -> m = v.toInt()
        "t" -> t = v.toInt()
        "p" -> p = v.toInt()
      }
    }
    return Triple(m, t, p)
  }
}
