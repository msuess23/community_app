package com.example.community_app.service

import com.example.community_app.util.MediaTargetType
import com.example.community_app.config.MediaConfig
import com.example.community_app.dto.MediaDto
import com.example.community_app.errors.BadRequestException
import com.example.community_app.errors.ForbiddenException
import com.example.community_app.errors.NotFoundException
import com.example.community_app.repository.*
import com.example.community_app.util.ensureViewAllowedForVisibility
import com.example.community_app.util.requireEditByCreatorOfficerOrAdmin
import io.ktor.http.content.*
import io.ktor.server.auth.jwt.*
import java.io.File
import java.util.*

data class MediaBinary(
  val file: File,
  val mimeType: String,
  val filename: String
)

/**
 * Generische Medien-Pipeline mit Policies je Zieltyp.
 * Aktuell: TICKET vollständig, USER (Avatar) vorbereitet.
 */
class MediaService(
  private val repo: MediaRepository,
  private val ticketRepo: TicketRepository
) {

  // ---------- Public/Authed reads ----------

  suspend fun list(
    targetType: MediaTargetType,
    targetId: Int,
    principal: JWTPrincipal?
  ): List<MediaDto> {
    authorizeView(targetType, targetId, principal)
    return repo.list(targetType, targetId).map { it.toDto() }
  }

  suspend fun getBinary(mediaId: Int, principal: JWTPrincipal?): MediaBinary {
    val rec = repo.findById(mediaId) ?: throw NotFoundException("Media not found")
    authorizeView(rec.targetType, rec.targetId, principal)
    val file = File(MediaConfig.targetDir(rec.targetType.name, rec.targetId), rec.filename)
    if (!file.exists()) throw NotFoundException("File missing on server")
    return MediaBinary(file = file, mimeType = rec.mimeType, filename = rec.originalFilename ?: rec.filename)
  }

  // ---------- Writes ----------

  suspend fun upload(
    targetType: MediaTargetType,
    targetId: Int,
    principal: JWTPrincipal,
    fileItem: PartData.FileItem
  ): MediaDto {
    authorizeWrite(targetType, targetId, principal)

    val contentType = fileItem.contentType?.toString()?.lowercase()
      ?: throw BadRequestException("Missing Content-Type")
    if (contentType !in MediaConfig.allowedMimeTypes) {
      throw BadRequestException("Unsupported media type. Allowed: ${MediaConfig.allowedMimeTypes.joinToString()}")
    }

    val originalName = fileItem.originalFileName
    val ext = when (contentType) {
      "image/jpeg" -> ".jpg"
      "image/png" -> ".png"
      else -> ".bin"
    }
    val serverName = UUID.randomUUID().toString().replace("-", "") + ext
    val dir = MediaConfig.targetDir(targetType.name, targetId)
    val outFile = File(dir, serverName)

    // Size guard + write
    var total: Long = 0
    outFile.outputStream().buffered().use { os ->
      fileItem.streamProvider().use { input ->
        val buf = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
          val read = input.read(buf)
          if (read <= 0) break
          total += read
          if (total > MediaConfig.maxFileSizeBytes) {
            try { os.flush(); os.close(); outFile.delete() } catch (_: Throwable) {}
            throw BadRequestException("File too large (max ${MediaConfig.maxFileSizeBytes} bytes)")
          }
          os.write(buf, 0, read)
        }
      }
    }

    val saved = repo.create(
      MediaCreateData(
        targetType = targetType,
        targetId = targetId,
        serverFilename = serverName,
        originalFilename = originalName,
        mimeType = contentType,
        sizeBytes = total
      )
    )
    return saved.toDto()
  }

  suspend fun delete(
    targetType: MediaTargetType,
    targetId: Int,
    mediaId: Int,
    principal: JWTPrincipal
  ) {
    authorizeWrite(targetType, targetId, principal)

    val media = repo.findById(mediaId) ?: throw NotFoundException("Media not found")
    if (media.targetType != targetType || media.targetId != targetId) {
      throw ForbiddenException("Media does not belong to target")
    }

    val file = File(MediaConfig.targetDir(targetType.name, targetId), media.filename)
    val ok = repo.delete(mediaId)
    if (ok && file.exists()) runCatching { file.delete() }
  }

  /** Für Löschvorgänge des Targets (z. B. Ticket) – DB & Files aufräumen. */
  suspend fun deleteAllForTarget(targetType: MediaTargetType, targetId: Int) {
    val list = repo.list(targetType, targetId)
    repo.deleteAll(targetType, targetId)
    list.forEach { rec ->
      val f = File(MediaConfig.targetDir(targetType.name, targetId), rec.filename)
      if (f.exists()) runCatching { f.delete() }
    }
    val dir = MediaConfig.targetDir(targetType.name, targetId)
    runCatching { if (dir.exists() && dir.isDirectory) dir.delete() }
  }

  // ---------- Policies per target ----------

  private suspend fun authorizeView(type: MediaTargetType, id: Int, principal: JWTPrincipal?) {
    when (type) {
      MediaTargetType.TICKET -> {
        val t = ticketRepo.findById(id) ?: throw NotFoundException("Ticket not found")
        ensureViewAllowedForVisibility(t.visibility, t.creatorUserId, t.officeId, principal)
      }
    }
  }

  private suspend fun authorizeWrite(type: MediaTargetType, id: Int, principal: JWTPrincipal) {
    when (type) {
      MediaTargetType.TICKET -> {
        val t = ticketRepo.findById(id) ?: throw NotFoundException("Ticket not found")
        requireEditByCreatorOfficerOrAdmin(principal, t.creatorUserId, t.officeId)
      }
    }
  }

  // ---------- mapping ----------

  private fun MediaRecord.toDto() = MediaDto(
    id = id,
    url = "/api/media/$id",
    mimeType = mimeType,
    width = width,
    height = height,
    sizeBytes = sizeBytes,
    createdAt = createdAt.toString()
  )
}
