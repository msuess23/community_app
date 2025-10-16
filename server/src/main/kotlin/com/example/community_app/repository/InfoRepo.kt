package com.example.community_app.repository

import com.example.community_app.util.InfoCategory
import com.example.community_app.dto.LocationDto
import com.example.community_app.model.*
import com.example.community_app.util.applyBbox
import com.example.community_app.util.createLocation
import com.example.community_app.util.updateFrom
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
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
  val location: LocationRecord?,
  val createdAt: Instant,
  val startsAt: Instant,
  val endsAt: Instant
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
  val location: LocationDto?,
  val startsAt: Instant,
  val endsAt: Instant
)

data class InfoUpdateData(
  val title: String?,
  val description: String?,
  val category: InfoCategory?,
  val officeId: Int?,
  val location: LocationDto?,
  val startsAt: Instant?,
  val endsAt: Instant?
)

object DefaultInfoRepository : InfoRepository {

  override suspend fun list(
    officeId: Int?, category: InfoCategory?, startsFrom: Instant?, endsTo: Instant?, bbox: DoubleArray?
  ): List<InfoRecord> = newSuspendedTransaction(Dispatchers.IO) {
    val base: Query = (Infos leftJoin Locations).selectAll()

    if (officeId != null) base.andWhere { Infos.office eq officeId }
    if (category != null) base.andWhere { Infos.category eq category }
    if (startsFrom != null) base.andWhere { Infos.startsAt greaterEq startsFrom }
    if (endsTo != null) base.andWhere { Infos.endsAt lessEq endsTo }
    base.applyBbox(bbox)

    base.orderBy(Infos.startsAt to SortOrder.ASC).map { it.toInfoRecord() }
  }

  override suspend fun findById(id: Int): InfoRecord? = newSuspendedTransaction(Dispatchers.IO) {
    (Infos leftJoin Locations)
      .selectAll()
      .apply { andWhere { Infos.id eq id } }
      .limit(1)
      .firstOrNull()
      ?.toInfoRecord()
  }

  override suspend fun create(data: InfoCreateData): InfoRecord = newSuspendedTransaction(Dispatchers.IO) {
    val locEntity = data.location?.let { createLocation(it) }
    val officeEntity = data.officeId?.let { OfficeEntity.findById(it) }

    val info = InfoEntity.new {
      title = data.title
      description = data.description
      category = data.category
      office = officeEntity
      location = locEntity
      startsAt = data.startsAt
      endsAt = data.endsAt
    }

    (Infos leftJoin Locations)
      .selectAll()
      .apply { andWhere { Infos.id eq info.id } }
      .first()
      .toInfoRecord()
  }

  override suspend fun update(id: Int, patch: InfoUpdateData): InfoRecord? = newSuspendedTransaction(Dispatchers.IO) {
    val info = InfoEntity.findById(id) ?: return@newSuspendedTransaction null

    patch.title?.let { info.title = it }
    patch.description?.let { info.description = it }
    patch.category?.let { info.category = it }
    if (patch.officeId != null) {
      info.office = OfficeEntity.findById(patch.officeId)
    }
    patch.location?.let {
      if (info.location == null) {
        info.location = createLocation(it)
      } else {
        info.location!!.updateFrom(it)
      }
    }
    patch.startsAt?.let { info.startsAt = it }
    patch.endsAt?.let { info.endsAt = it }

    (Infos leftJoin Locations)
      .selectAll()
      .apply { andWhere { Infos.id eq info.id } }
      .first()
      .toInfoRecord()
  }

  override suspend fun delete(id: Int): Boolean = newSuspendedTransaction(Dispatchers.IO) {
    val entity = InfoEntity.findById(id) ?: return@newSuspendedTransaction false
    entity.delete()
    true
  }

  // --- mapping helper ---

  private fun ResultRow.toInfoRecord(): InfoRecord {
    val locId: EntityID<Int>? = this[Infos.location]
    val loc = if (locId != null) {
      LocationRecord(
        id = locId.value,
        longitude = this[Locations.longitude],
        latitude = this[Locations.latitude],
        altitude = this[Locations.altitude],
        accuracy = this[Locations.accuracy]
      )
    } else null

    return InfoRecord(
      id = this[Infos.id].value,
      title = this[Infos.title],
      description = this[Infos.description],
      category = this[Infos.category],
      officeId = this[Infos.office]?.value,
      location = loc,
      createdAt = this[Infos.createdAt],
      startsAt = this[Infos.startsAt],
      endsAt = this[Infos.endsAt]
    )
  }
}
