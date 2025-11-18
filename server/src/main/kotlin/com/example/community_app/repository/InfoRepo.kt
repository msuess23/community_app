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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

data class InfoRecord(
  val id: Int,
  val title: String,
  val description: String?,
  val category: InfoCategory,
  val officeId: Int?,
  val address: AddressRecord?,
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
  val address: AddressDto?,
  val startsAt: Instant,
  val endsAt: Instant
)

data class InfoUpdateData(
  val title: String?,
  val description: String?,
  val category: InfoCategory?,
  val officeId: Int?,
  val address: AddressDto?,
  val startsAt: Instant?,
  val endsAt: Instant?
)

object DefaultInfoRepository : InfoRepository {

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

    // Media fetch
    val ids = rows.map { it[Infos.id].value }
    val mediaMap = fetchFirstMediaMap(MediaTargetType.INFO, ids)

    rows.map { it.toInfoRecord(mediaMap[it[Infos.id].value]) }
  }

  override suspend fun findById(id: Int): InfoRecord? = newSuspendedTransaction(Dispatchers.IO) {
    val row = (Infos leftJoin Addresses)
      .selectAll()
      .apply { andWhere { Infos.id eq id } }
      .limit(1)
      .firstOrNull() ?: return@newSuspendedTransaction null

    val img = fetchFirstMediaUrl(MediaTargetType.INFO, id)
    row.toInfoRecord(img)
  }

  override suspend fun create(data: InfoCreateData): InfoRecord = newSuspendedTransaction(Dispatchers.IO) {
    val addrEntity = data.address?.let { createAddress(it) }
    val officeEntity = data.officeId?.let { OfficeEntity.findById(it) }

    val info = InfoEntity.new {
      title = data.title
      description = data.description
      category = data.category
      office = officeEntity
      address = addrEntity
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
        info.address!!.updateFrom(it)
      }
    }
    patch.startsAt?.let { info.startsAt = it }
    patch.endsAt?.let { info.endsAt = it }

    val img = fetchFirstMediaUrl(MediaTargetType.INFO, id)
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

  // --- helpers ---

  private fun fetchFirstMediaUrl(type: MediaTargetType, targetId: Int): String? {
    val m = MediaEntity.find { (Media.targetType eq type) and (Media.targetId eq targetId) }
      .orderBy(Media.createdAt to SortOrder.DESC)
      .limit(1)
      .firstOrNull()
    return m?.let { "/api/media/${it.id.value}" }
  }

  private fun fetchFirstMediaMap(type: MediaTargetType, ids: List<Int>): Map<Int, String> {
    val all = MediaEntity.find { (Media.targetType eq type) and (Media.targetId inList ids) }
      .orderBy(Media.createdAt to SortOrder.DESC)
    val map = mutableMapOf<Int, String>()
    all.forEach { m ->
      if (!map.containsKey(m.targetId)) {
        map[m.targetId] = "/api/media/${m.id.value}"
      }
    }
    return map
  }

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
      address = addr,
      createdAt = this[Infos.createdAt],
      startsAt = this[Infos.startsAt],
      endsAt = this[Infos.endsAt],
      imageUrl = imageUrl
    )
  }
}