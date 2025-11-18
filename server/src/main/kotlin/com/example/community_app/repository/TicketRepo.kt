package com.example.community_app.repository

import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
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

data class TicketRecord(
  val id: Int,
  val title: String,
  val description: String?,
  val category: TicketCategory,
  val officeId: Int?,
  val creatorUserId: Int,
  val address: AddressRecord?,
  val visibility: TicketVisibility,
  val createdAt: Instant,
  val imageUrl: String? // Titelbild
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
  val address: AddressDto?,
  val visibility: TicketVisibility
)

data class TicketUpdateData(
  val title: String?,
  val description: String?,
  val category: TicketCategory?,
  val officeId: Int?,
  val address: AddressDto?,
  val visibility: TicketVisibility?
)

object DefaultTicketRepository : TicketRepository {

  override suspend fun listPublic(
    officeId: Int?, category: TicketCategory?, createdFrom: Instant?, createdTo: Instant?, bbox: DoubleArray?
  ): List<TicketRecord> = newSuspendedTransaction(Dispatchers.IO) {
    val base: Query = (Tickets leftJoin Addresses).selectAll()
    base.andWhere { Tickets.visibility eq TicketVisibility.PUBLIC }
    if (officeId != null) base.andWhere { Tickets.office eq officeId }
    if (category != null) base.andWhere { Tickets.category eq category }
    if (createdFrom != null) base.andWhere { Tickets.createdAt greaterEq createdFrom }
    if (createdTo != null) base.andWhere { Tickets.createdAt lessEq createdTo }
    base.applyBbox(bbox)

    val records = base.orderBy(Tickets.createdAt to SortOrder.DESC).toList()
    if (records.isEmpty()) return@newSuspendedTransaction emptyList()

    // Bulk-Fetch Image URLs (erstes Bild pro Ticket)
    val ticketIds = records.map { it[Tickets.id].value }
    val mediaMap = fetchFirstMediaMap(MediaTargetType.TICKET, ticketIds)

    records.map { it.toRecord(mediaMap[it[Tickets.id].value]) }
  }

  override suspend fun findById(id: Int): TicketRecord? = newSuspendedTransaction(Dispatchers.IO) {
    val row = (Tickets leftJoin Addresses)
      .selectAll()
      .apply { andWhere { Tickets.id eq id } }
      .limit(1)
      .firstOrNull() ?: return@newSuspendedTransaction null

    val mediaUrl = fetchFirstMediaUrl(MediaTargetType.TICKET, id)
    row.toRecord(mediaUrl)
  }

  override suspend fun create(data: TicketCreateData): TicketRecord = newSuspendedTransaction(Dispatchers.IO) {
    val creator = UserEntity.findById(data.creatorUserId) ?: error("Creator not found")
    val office = OfficeEntity.findById(data.officeId) ?: error("Office not found")
    val addrEntity = data.address?.let { createAddress(it) }
    val ticket = TicketEntity.new {
      title = data.title
      description = data.description
      category = data.category
      this.creator = creator
      this.office = office
      this.address = addrEntity
      visibility = data.visibility
    }
    // Neues Ticket hat noch keine Bilder
    (Tickets leftJoin Addresses)
      .selectAll().apply { andWhere { Tickets.id eq ticket.id } }
      .first()
      .toRecord(null)
  }

  override suspend fun update(id: Int, patch: TicketUpdateData): TicketRecord? = newSuspendedTransaction(Dispatchers.IO) {
    val t = TicketEntity.findById(id) ?: return@newSuspendedTransaction null
    patch.title?.let { t.title = it }
    patch.description?.let { t.description = it }
    patch.category?.let { t.category = it }
    patch.officeId?.let { t.office = OfficeEntity.findById(it) }
    patch.address?.let {
      if (t.address == null) {
        t.address = createAddress(it)
      } else {
        t.address!!.updateFrom(it)
      }
    }
    patch.visibility?.let { t.visibility = it }

    val mediaUrl = fetchFirstMediaUrl(MediaTargetType.TICKET, id)
    (Tickets leftJoin Addresses)
      .selectAll().apply { andWhere { Tickets.id eq t.id } }
      .first()
      .toRecord(mediaUrl)
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
    TicketVoteEntity.new { this.ticket = ticket; this.user = user }
    true
  }
  override suspend fun removeVote(ticketId: Int, userId: Int): Boolean = newSuspendedTransaction(Dispatchers.IO) {
    val existing = TicketVoteEntity.find { (TicketVotes.ticket eq ticketId) and (TicketVotes.user eq userId) }.firstOrNull() ?: return@newSuspendedTransaction false
    existing.delete()
    true
  }

  // --- helpers ---

  private fun fetchFirstMediaUrl(type: MediaTargetType, targetId: Int): String? {
    val m = MediaEntity.find { (Media.targetType eq type) and (Media.targetId eq targetId) }
      .orderBy(Media.createdAt to SortOrder.DESC) // oder ASC für das älteste als Titelbild
      .limit(1)
      .firstOrNull()
    return m?.let { "/api/media/${it.id.value}" }
  }

  private fun fetchFirstMediaMap(type: MediaTargetType, ids: List<Int>): Map<Int, String> {
    // Naiv: fetch all for these IDs, then group. Optimierung: Subqueries.
    // Da wir nur das Titelbild wollen:
    val all = MediaEntity.find { (Media.targetType eq type) and (Media.targetId inList ids) }
      .orderBy(Media.createdAt to SortOrder.DESC)

    val map = mutableMapOf<Int, String>()
    // Iteriere rückwärts oder filtere, um nur das "Top"-Element zu behalten
    all.forEach { m ->
      // putIfAbsent würde das älteste behalten (bei DESC ist das erste das neueste)
      // Entscheide Strategie: Neuestes als Titelbild?
      if (!map.containsKey(m.targetId)) {
        map[m.targetId] = "/api/media/${m.id.value}"
      }
    }
    return map
  }

  private fun ResultRow.toRecord(imageUrl: String?): TicketRecord {
    val addrId: EntityID<Int>? = this[Tickets.address]
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

    return TicketRecord(
      id = this[Tickets.id].value,
      title = this[Tickets.title],
      description = this[Tickets.description],
      category = this[Tickets.category],
      officeId = this[Tickets.office]?.value,
      creatorUserId = this[Tickets.creator].value,
      address = addr,
      visibility = this[Tickets.visibility],
      createdAt = this[Tickets.createdAt],
      imageUrl = imageUrl
    )
  }
}