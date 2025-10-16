package com.example.community_app.repository

import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import com.example.community_app.dto.LocationDto
import com.example.community_app.model.*
import com.example.community_app.util.applyBbox
import com.example.community_app.util.createLocation
import com.example.community_app.util.updateFrom
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

data class TicketRecord(
  val id: Int,
  val title: String,
  val description: String?,
  val category: TicketCategory,
  val officeId: Int?,
  val creatorUserId: Int,
  val location: LocationRecord?,
  val visibility: TicketVisibility,
  val createdAt: Instant
)

interface TicketRepository {
  suspend fun listPublic(
    officeId: Int?,
    category: TicketCategory?,
    createdFrom: Instant?,
    createdTo: Instant?,
    bbox: DoubleArray?
  ): List<TicketRecord>

  suspend fun findById(id: Int): TicketRecord?
  suspend fun create(data: TicketCreateData): TicketRecord
  suspend fun update(id: Int, patch: TicketUpdateData): TicketRecord?
  suspend fun delete(id: Int): Boolean

  // votes
  suspend fun countVotes(ticketId: Int): Int
  suspend fun userHasVoted(ticketId: Int, userId: Int): Boolean
  suspend fun addVote(ticketId: Int, userId: Int): Boolean
  suspend fun removeVote(ticketId: Int, userId: Int): Boolean
}

data class TicketCreateData(
  val title: String,
  val description: String?,
  val category: TicketCategory,
  val officeId: Int,
  val creatorUserId: Int,
  val location: LocationDto?,
  val visibility: TicketVisibility
)

data class TicketUpdateData(
  val title: String?,
  val description: String?,
  val category: TicketCategory?,
  val officeId: Int?,
  val location: LocationDto?,
  val visibility: TicketVisibility?
)

object DefaultTicketRepository : TicketRepository {

  override suspend fun listPublic(
    officeId: Int?, category: TicketCategory?, createdFrom: Instant?, createdTo: Instant?, bbox: DoubleArray?
  ): List<TicketRecord> = newSuspendedTransaction(Dispatchers.IO) {
    val base: Query = (Tickets leftJoin Locations).selectAll()
    base.andWhere { Tickets.visibility eq TicketVisibility.PUBLIC }
    if (officeId != null) base.andWhere { Tickets.office eq officeId }
    if (category != null) base.andWhere { Tickets.category eq category }
    if (createdFrom != null) base.andWhere { Tickets.createdAt greaterEq createdFrom }
    if (createdTo != null) base.andWhere { Tickets.createdAt lessEq createdTo }
    base.applyBbox(bbox)
    base.orderBy(Tickets.createdAt to SortOrder.DESC).map { it.toRecord() }
  }

  override suspend fun findById(id: Int): TicketRecord? = newSuspendedTransaction(Dispatchers.IO) {
    (Tickets leftJoin Locations)
      .selectAll()
      .apply { andWhere { Tickets.id eq id } }
      .limit(1)
      .firstOrNull()
      ?.toRecord()
  }

  override suspend fun create(data: TicketCreateData): TicketRecord = newSuspendedTransaction(Dispatchers.IO) {
    val creator = UserEntity.findById(data.creatorUserId) ?: error("Creator not found")
    val office = OfficeEntity.findById(data.officeId) ?: error("Office not found")
    val locEntity = data.location?.let { createLocation(it) }
    val ticket = TicketEntity.new {
      title = data.title
      description = data.description
      category = data.category
      this.creator = creator
      this.office = office
      this.location = locEntity
      visibility = data.visibility
    }
    (Tickets leftJoin Locations)
      .selectAll()
      .apply { andWhere { Tickets.id eq ticket.id } }
      .first()
      .toRecord()
  }

  override suspend fun update(id: Int, patch: TicketUpdateData): TicketRecord? = newSuspendedTransaction(Dispatchers.IO) {
    val t = TicketEntity.findById(id) ?: return@newSuspendedTransaction null
    patch.title?.let { t.title = it }
    patch.description?.let { t.description = it }
    patch.category?.let { t.category = it }
    patch.officeId?.let { t.office = OfficeEntity.findById(it) }
    patch.location?.let {
      if (t.location == null) {
        t.location = createLocation(it)
      } else {
        t.location!!.updateFrom(it)
      }
    }
    patch.visibility?.let { t.visibility = it }

    (Tickets leftJoin Locations)
      .selectAll()
      .apply { andWhere { Tickets.id eq t.id } }
      .first()
      .toRecord()
  }

  override suspend fun delete(id: Int): Boolean = newSuspendedTransaction(Dispatchers.IO) {
    val t = TicketEntity.findById(id) ?: return@newSuspendedTransaction false
    t.delete()
    true
  }

  // --- votes ---

  override suspend fun countVotes(ticketId: Int): Int = newSuspendedTransaction(Dispatchers.IO) {
    TicketVoteEntity.find { TicketVotes.ticket eq ticketId }.count().toInt()
  }

  override suspend fun userHasVoted(ticketId: Int, userId: Int): Boolean = newSuspendedTransaction(Dispatchers.IO) {
    TicketVoteEntity.find { (TicketVotes.ticket eq ticketId) and (TicketVotes.user eq userId) }.empty().not()
  }

  override suspend fun addVote(ticketId: Int, userId: Int): Boolean = newSuspendedTransaction(Dispatchers.IO) {
    val ticket = TicketEntity.findById(ticketId) ?: return@newSuspendedTransaction false
    val user = UserEntity.findById(userId) ?: return@newSuspendedTransaction false
    val exists = TicketVoteEntity.find { (TicketVotes.ticket eq ticketId) and (TicketVotes.user eq userId) }.empty().not()
    if (exists) return@newSuspendedTransaction false
    TicketVoteEntity.new {
      this.ticket = ticket
      this.user = user
    }
    true
  }

  override suspend fun removeVote(ticketId: Int, userId: Int): Boolean = newSuspendedTransaction(Dispatchers.IO) {
    val existing = TicketVoteEntity.find { (TicketVotes.ticket eq ticketId) and (TicketVotes.user eq userId) }.firstOrNull()
      ?: return@newSuspendedTransaction false
    existing.delete()
    true
  }

  // --- mapping ---

  private fun ResultRow.toRecord(): TicketRecord {
    val locId: EntityID<Int>? = this[Tickets.location]
    val loc = if (locId != null) {
      LocationRecord(
        id = locId.value,
        longitude = this[Locations.longitude],
        latitude = this[Locations.latitude],
        altitude = this[Locations.altitude],
        accuracy = this[Locations.accuracy]
      )
    } else null

    return TicketRecord(
      id = this[Tickets.id].value,
      title = this[Tickets.title],
      description = this[Tickets.description],
      category = this[Tickets.category],
      officeId = this[Tickets.office]?.value,
      creatorUserId = this[Tickets.creator].value,
      location = loc,
      visibility = this[Tickets.visibility],
      createdAt = this[Tickets.createdAt]
    )
  }
}
