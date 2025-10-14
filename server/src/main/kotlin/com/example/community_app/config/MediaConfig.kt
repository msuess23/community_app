package com.example.community_app.config

import io.ktor.server.config.*
import java.io.File

object MediaConfig {
  lateinit var mediaRoot: File
    private set

  var maxFileSizeBytes: Long = 10L * 1024 * 1024
    private set
  val allowedMimeTypes = setOf("image/jpeg", "image/png")

  fun init(config: ApplicationConfig) {
    val mediaConfig = config.config("ktor.media")
    val rootPath = mediaConfig.propertyOrNull("root")?.getString() ?: "build/media"
    mediaRoot = File(rootPath).absoluteFile
    if (!mediaRoot.exists()) mediaRoot.mkdirs()

    maxFileSizeBytes = mediaConfig.propertyOrNull("maxFileSizeBytes")?.getString()?.toLongOrNull()
      ?: maxFileSizeBytes
  }

  /** Verzeichnis für ein Ziel (Type/ID), z. B. TICKET/42 -> build/media/tickets/42 */
  fun targetDir(targetType: String, targetId: Int): File {
    val base = when (targetType.lowercase()) {
      "ticket" -> "tickets"
      "user" -> "users"
      else -> targetType.lowercase()
    }
    val dir = File(mediaRoot, "$base/$targetId")
    if (!dir.exists()) dir.mkdirs()
    return dir
  }
}
