package com.example.community_app.repository

import com.example.community_app.dto.LocationDto
import com.example.community_app.model.*
import com.example.community_app.util.applyBbox
import com.example.community_app.util.createLocation
import com.example.community_app.util.updateFrom
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

data class LocationRecord(
  val id: Int,
  val longitude: Double,
  val latitude: Double,
  val altitude: Double?,
  val accuracy: Double?
)

data class OfficeRecord(
  val id: Int,
  val name: String,
  val description: String?,
  val services: String?,
  val openingHours: String?,
  val contactEmail: String?,
  val phone: String?,
  val location: LocationRecord,
  val createdAt: java.time.Instant
)

interface OfficeRepository {
  suspend fun list(
    q: String? = null,
    bbox: DoubleArray? = null // [minLon, minLat, maxLon, maxLat]
  ): List<OfficeRecord>

  suspend fun findById(id: Int): OfficeRecord?
  suspend fun create(dto: OfficeCreateData): OfficeRecord
  suspend fun update(id: Int, patch: OfficeUpdateData): OfficeRecord?
  suspend fun delete(id: Int)
}

data class OfficeCreateData(
  val name: String,
  val description: String?,
  val services: String?,
  val openingHours: String?,
  val contactEmail: String?,
  val phone: String?,
  val location: LocationDto
)

data class OfficeUpdateData(
  val name: String?,
  val description: String?,
  val services: String?,
  val openingHours: String?,
  val contactEmail: String?,
  val phone: String?,
  val location: LocationDto?
)

object DefaultOfficeRepository : OfficeRepository {

  override suspend fun list(q: String?, bbox: DoubleArray?): List<OfficeRecord> =
    newSuspendedTransaction(Dispatchers.IO) {
      val base: Query = (Offices innerJoin Locations).selectAll()

      val filtered = base.apply {
        if (!q.isNullOrBlank()) {
          andWhere {
            (Offices.name like "%$q%") or
                (Offices.description like "%$q%") or
                (Offices.services like "%$q%")
          }
        }
        applyBbox(bbox)
      }

      filtered
        .orderBy(Offices.createdAt to SortOrder.DESC)
        .map { row -> row.toOfficeRecord() }
    }

  override suspend fun findById(id: Int): OfficeRecord? =
    newSuspendedTransaction(Dispatchers.IO) {
      (Offices innerJoin Locations)
        .selectAll()
        .apply { andWhere { Offices.id eq id } }
        .limit(1)
        .firstOrNull()
        ?.toOfficeRecord()
    }

  override suspend fun create(dto: OfficeCreateData): OfficeRecord =
    newSuspendedTransaction(Dispatchers.IO) {
      val loc = createLocation(dto.location)
      val office = OfficeEntity.new {
        name = dto.name
        description = dto.description
        services = dto.services
        openingHours = dto.openingHours
        contactEmail = dto.contactEmail
        phone = dto.phone
        location = loc
      }
      (Offices innerJoin Locations)
        .selectAll().apply { andWhere { Offices.id eq office.id } }
        .first().toOfficeRecord()
    }

  override suspend fun update(id: Int, patch: OfficeUpdateData): OfficeRecord? =
    newSuspendedTransaction(Dispatchers.IO) {
      val office = OfficeEntity.findById(id) ?: return@newSuspendedTransaction null

      patch.name?.let { office.name = it }
      patch.description?.let { office.description = it }
      patch.services?.let { office.services = it }
      patch.openingHours?.let { office.openingHours = it }
      patch.contactEmail?.let { office.contactEmail = it }
      patch.phone?.let { office.phone = it }
      patch.location?.let { office.location.updateFrom(it) }

      (Offices innerJoin Locations)
        .selectAll().apply { andWhere { Offices.id eq office.id } }
        .first().toOfficeRecord()
    }

  override suspend fun delete(id: Int) =
    newSuspendedTransaction(Dispatchers.IO) {
      val office = OfficeEntity.findById(id) ?: return@newSuspendedTransaction
      office.delete()
    }

  // --- mapping ---

  private fun ResultRow.toOfficeRecord(): OfficeRecord {
    val locId: EntityID<Int> = this[Offices.location]
    val loc = LocationRecord(
      id = locId.value,
      longitude = this[Locations.longitude],
      latitude = this[Locations.latitude],
      altitude = this[Locations.altitude],
      accuracy = this[Locations.accuracy]
    )
    return OfficeRecord(
      id = this[Offices.id].value,
      name = this[Offices.name],
      description = this[Offices.description],
      services = this[Offices.services],
      openingHours = this[Offices.openingHours],
      contactEmail = this[Offices.contactEmail],
      phone = this[Offices.phone],
      location = loc,
      createdAt = this[Offices.createdAt]
    )
  }
}
