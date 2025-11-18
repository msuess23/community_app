package com.example.community_app.model

import com.example.community_app.util.MediaTargetType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Media : IntIdTable(name = "MEDIA") {
  val targetType = enumerationByName("TARGET_TYPE", 32, MediaTargetType::class)
  val targetId = integer("TARGET_ID")
  val filename = varchar("FILENAME", 255)
  val originalFilename = varchar("ORIGINAL_FILENAME", 255).nullable()
  val mimeType = varchar("MIME_TYPE", 100)
  val sizeBytes = long("SIZE_BYTES")
  val width = integer("WIDTH").nullable()
  val height = integer("HEIGHT").nullable()
  val createdAt = timestamp("CREATED_AT").defaultExpression(CurrentTimestamp)
  val isCover = bool("IS_COVER").default(false)

  init {
    index(true, targetType, targetId, filename) // unique je Ziel + Dateiname
    index(false, targetType, targetId)
  }
}

class MediaEntity(id: org.jetbrains.exposed.dao.id.EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<MediaEntity>(Media)
  var targetType by Media.targetType
  var targetId by Media.targetId
  var filename by Media.filename
  var originalFilename by Media.originalFilename
  var mimeType by Media.mimeType
  var sizeBytes by Media.sizeBytes
  var width by Media.width
  var height by Media.height
  var createdAt by Media.createdAt
  var isCover by Media.isCover
}
