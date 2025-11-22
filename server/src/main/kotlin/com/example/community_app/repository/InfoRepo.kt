package com.example.community_app.repository

import com.example.community_app.util.InfoCategory
import com.example.community_app.util.MediaTargetType
import com.example.community_app.dto.AddressDto
import com.example.community_app.model.*
import com.example.community_app.util.applyBbox
import com.example.community_app.util.createAddress
import com.example.community_app.util.updateFrom
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

data class InfoRecord(
  val id: Int,
  val title: String,
  val description: String?,
  val category: InfoCategory,
  val officeId: Int?,
  val address: AddressRecord?, // LocationRecord replaced by AddressRecord
  val createdAt: Instant,
  val startsAt: Instant,
  val endsAt: Instant,
  val imageUrl: String?
)

interface InfoRepository {
  suspend fun list(
    officeId: Int?,
    category: InfoCategory?,
    startsFrom: Instant?,
    endsTo: Instant?,
    bbox: DoubleArray?
  ): List<InfoRecord>

  suspend fun findById(id: Int): InfoRecord?
  suspend fun create(data: InfoCreateData): InfoRecord
  suspend fun update(id: Int, patch: InfoUpdateData): InfoRecord?
  suspend fun delete(id: Int): Boolean
}

data class InfoCreateData(
  val title: String,
  val description: String?,
  val category: InfoCategory,
  val officeId: Int?,
  val address: AddressDto?, // LocationDto replaced by AddressDto
  val startsAt: Instant,
  val endsAt: Instant
)

data class InfoUpdateData(
  val title: String?,
  val description: String?,
  val category: InfoCategory?,
  val officeId: Int?,
  val address: AddressDto?, // LocationDto replaced by AddressDto
  val startsAt: Instant?,
  val endsAt: Instant?
)

object DefaultInfoRepository : InfoRepository {

  private val mediaRepo = DefaultMediaRepository

  override suspend fun list(
    officeId: Int?, category: InfoCategory?, startsFrom: Instant?, endsTo: Instant?, bbox: DoubleArray?
  ): List<InfoRecord> = newSuspendedTransaction(Dispatchers.IO) {
    val base: Query = (Infos leftJoin Addresses).selectAll()

    if (officeId != null) base.andWhere { Infos.office eq officeId }
    if (category != null) base.andWhere { Infos.category eq category }
    if (startsFrom != null) base.andWhere { Infos.startsAt greaterEq startsFrom }
    if (endsTo != null) base.andWhere { Infos.endsAt lessEq endsTo }
    base.applyBbox(bbox)

    val rows = base.orderBy(Infos.startsAt to SortOrder.ASC).toList()
    if (rows.isEmpty()) return@newSuspendedTransaction emptyList()

    // Bulk-Fetch Cover Image URLs
    val ids = rows.map { it[Infos.id].value }
    val mediaMap = fetchCoverMediaMap(MediaTargetType.INFO, ids)

    rows.map { it.toInfoRecord(mediaMap[it[Infos.id].value]) }
  }

  override suspend fun findById(id: Int): InfoRecord? = newSuspendedTransaction(Dispatchers.IO) {
    val row = (Infos leftJoin Addresses)
      .selectAll()
      .apply { andWhere { Infos.id eq id } }
      .limit(1)
      .firstOrNull() ?: return@newSuspendedTransaction null

    val img = fetchCoverMediaUrl(MediaTargetType.INFO, id)
    row.toInfoRecord(img)
  }

  override suspend fun create(data: InfoCreateData): InfoRecord = newSuspendedTransaction(Dispatchers.IO) {
    val addrEntity = data.address?.let { createAddress(it) } // using createAddress
    val officeEntity = data.officeId?.let { OfficeEntity.findById(it) }

    val info = InfoEntity.new {
      title = data.title
      description = data.description
      category = data.category
      office = officeEntity
      address = addrEntity // location now refers to AddressEntity
      startsAt = data.startsAt
      endsAt = data.endsAt
    }

    (Infos leftJoin Addresses)
      .selectAll()
      .apply { andWhere { Infos.id eq info.id } }
      .first()
      .toInfoRecord(null)
  }

  override suspend fun update(id: Int, patch: InfoUpdateData): InfoRecord? = newSuspendedTransaction(Dispatchers.IO) {
    val info = InfoEntity.findById(id) ?: return@newSuspendedTransaction null

    patch.title?.let { info.title = it }
    patch.description?.let { info.description = it }
    patch.category?.let { info.category = it }
    if (patch.officeId != null) {
      info.office = OfficeEntity.findById(patch.officeId)
    }
    patch.address?.let {
      if (info.address == null) {
        info.address = createAddress(it)
      } else {
        info.address!!.updateFrom(it) // using updateFrom (Address)
      }
    }
    patch.startsAt?.let { info.startsAt = it }
    patch.endsAt?.let { info.endsAt = it }

    val img = fetchCoverMediaUrl(MediaTargetType.INFO, id)
    (Infos leftJoin Addresses)
      .selectAll()
      .apply { andWhere { Infos.id eq info.id } }
      .first()
      .toInfoRecord(img)
  }

  override suspend fun delete(id: Int): Boolean = newSuspendedTransaction(Dispatchers.IO) {
    val entity = InfoEntity.findById(id) ?: return@newSuspendedTransaction false
    entity.delete()
    true
  }

  // --- media helpers ---

  private suspend fun fetchCoverMediaUrl(type: MediaTargetType, targetId: Int): String? {
    return mediaRepo.getCoverMedia(type, targetId)?.let { "/api/media/${it.id}" }
  }

  private suspend fun fetchCoverMediaMap(type: MediaTargetType, ids: List<Int>): Map<Int, String> {
    val allMedia = MediaEntity.find { (Media.targetType eq type) and (Media.targetId inList ids) }
      .orderBy(Media.createdAt to SortOrder.DESC)
      .toList()

    val coverMap = allMedia
      .filter { it.isCover }
      .associate { it.targetId to "/api/media/${it.id.value}" }
      .toMutableMap()

    // Fallback: Neuestes Bild für alle ohne explizites Cover
    allMedia
      .filter { it.targetId !in coverMap }
      .groupBy { it.targetId }
      .forEach { (targetId, mediaList) ->
        // mediaList is already sorted DESC by createdAt, so the first is the newest
        val newest = mediaList.first()
        coverMap[targetId] = "/api/media/${newest.id.value}"
      }

    return coverMap
  }

  // --- mapping helper ---

  private fun ResultRow.toInfoRecord(imageUrl: String?): InfoRecord {
    val addrId: EntityID<Int>? = this[Infos.address]
    val addr = if (addrId != null) {
      AddressRecord(
        id = addrId.value,
        street = this[Addresses.street],
        houseNumber = this[Addresses.houseNumber],
        zipCode = this[Addresses.zipCode],
        city = this[Addresses.city],
        longitude = this[Addresses.longitude],
        latitude = this[Addresses.latitude]
      )
    } else null

    return InfoRecord(
      id = this[Infos.id].value,
      title = this[Infos.title],
      description = this[Infos.description],
      category = this[Infos.category],
      officeId = this[Infos.office]?.value,
      address = addr, // Location replaced by Address
      createdAt = this[Infos.createdAt],
      startsAt = this[Infos.startsAt],
      endsAt = this[Infos.endsAt],
      imageUrl = imageUrl
    )
  }
}