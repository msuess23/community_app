package com.example.community_app.repository

import com.example.community_app.util.MediaTargetType
import com.example.community_app.model.Media
import com.example.community_app.model.MediaEntity
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

data class MediaRecord(
  val id: Int,
  val targetType: MediaTargetType,
  val targetId: Int,
  val filename: String,
  val originalFilename: String?,
  val mimeType: String,
  val sizeBytes: Long,
  val width: Int?,
  val height: Int?,
  val createdAt: Instant,
  val isCover: Boolean
)

data class MediaCreateData(
  val targetType: MediaTargetType,
  val targetId: Int,
  val serverFilename: String,
  val originalFilename: String?,
  val mimeType: String,
  val sizeBytes: Long,
  val width: Int? = null,
  val height: Int? = null,
  val isCover: Boolean = false
)

interface MediaRepository {
  suspend fun list(targetType: MediaTargetType, targetId: Int): List<MediaRecord>
  suspend fun findById(mediaId: Int): MediaRecord?
  suspend fun create(data: MediaCreateData): MediaRecord
  suspend fun delete(mediaId: Int): Boolean
  suspend fun deleteAll(targetType: MediaTargetType, targetId: Int): Int
  suspend fun setCover(mediaId: Int, targetType: MediaTargetType, targetId: Int): Boolean
  suspend fun getCoverMedia(targetType: MediaTargetType, targetId: Int ): MediaRecord?
}

object DefaultMediaRepository : MediaRepository {

  override suspend fun list(targetType: MediaTargetType, targetId: Int): List<MediaRecord> =
    newSuspendedTransaction(Dispatchers.IO) {
      MediaEntity.find { (Media.targetType eq targetType) and (Media.targetId eq targetId) }
        .orderBy(Media.createdAt to SortOrder.DESC)
        .map { it.toRecord() }
    }

  override suspend fun findById(mediaId: Int): MediaRecord? =
    newSuspendedTransaction(Dispatchers.IO) {
      MediaEntity.findById(mediaId)?.toRecord()
    }

  override suspend fun getCoverMedia(targetType: MediaTargetType, targetId: Int): MediaRecord? =
    newSuspendedTransaction(Dispatchers.IO) {
      val cover = MediaEntity.find {
        (Media.targetType eq targetType) and (Media.targetId eq targetId) and (Media.isCover eq true)
      }.limit(1).firstOrNull()

      if (cover != null) return@newSuspendedTransaction cover.toRecord()

      MediaEntity.find {
        (Media.targetType eq targetType) and (Media.targetId eq targetId)
      }.orderBy(Media.createdAt to SortOrder.DESC)
        .limit(1)
        .firstOrNull()
        ?.toRecord()
    }

  override suspend fun create(data: MediaCreateData): MediaRecord =
    newSuspendedTransaction(Dispatchers.IO) {
      if (data.isCover) {
        Media.update({ (Media.targetType eq data.targetType) and (Media.targetId eq data.targetId) and (Media.isCover eq true) }) {
          it[isCover] = false
        }
      }

      MediaEntity.new {
        this.targetType = data.targetType
        this.targetId = data.targetId
        this.filename = data.serverFilename
        this.originalFilename = data.originalFilename
        this.mimeType = data.mimeType
        this.sizeBytes = data.sizeBytes
        this.width = data.width
        this.height = data.height
        this.isCover = data.isCover
      }.toRecord()
    }

  override suspend fun setCover(mediaId: Int, targetType: MediaTargetType, targetId: Int): Boolean =
    newSuspendedTransaction(Dispatchers.IO) {
      val media = MediaEntity.findById(mediaId) ?: return@newSuspendedTransaction false

      if (media.targetType != targetType || media.targetId != targetId) return@newSuspendedTransaction false

      Media.update({ (Media.targetType eq targetType) and (Media.targetId eq targetId) and (Media.id neq mediaId) }) {
        it[isCover] = false
      }

      media.isCover = true
      true
    }

  override suspend fun delete(mediaId: Int): Boolean =
    newSuspendedTransaction(Dispatchers.IO) {
      val e = MediaEntity.findById(mediaId) ?: return@newSuspendedTransaction false
      e.delete()
      true
    }

  override suspend fun deleteAll(targetType: MediaTargetType, targetId: Int): Int =
    newSuspendedTransaction(Dispatchers.IO) {
      val all = MediaEntity.find { (Media.targetType eq targetType) and (Media.targetId eq targetId) }.toList()
      all.forEach { it.delete() }
      all.size
    }

  private fun MediaEntity.toRecord() = MediaRecord(
    id = id.value,
    targetType = targetType,
    targetId = targetId,
    filename = filename,
    originalFilename = originalFilename,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    width = width,
    height = height,
    createdAt = createdAt,
    isCover = isCover
  )
}
