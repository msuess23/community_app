package com.example.community_app.repository

import com.example.community_app.model.AppointmentEntity
import com.example.community_app.model.Appointments
import com.example.community_app.model.OfficeEntity
import com.example.community_app.model.UserEntity
import com.example.community_app.model.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.time.Instant

data class AppointmentRecord(
  val id: Int,
  val officeId: Int,
  val userId: Int?,            // null = Slot ist frei
  val startsAt: Instant,
  val endsAt: Instant
)

interface AppointmentRepository {
  suspend fun listFreeSlots(
    officeId: Int,
    from: Instant? = null,
    to: Instant? = null
  ): List<AppointmentRecord>

  suspend fun createSlots(
    officeId: Int,
    slots: List<Pair<Instant, Instant>>
  ): List<AppointmentRecord>

  /** Löscht nur freie Slots (userId == null). */
  suspend fun deleteSlot(officeId: Int, slotId: Int): Boolean

  /** Slot nach ID lesen. */
  suspend fun findById(id: Int): AppointmentRecord?

  /** Slot per ID buchen. Gibt das gebuchte Appointment zurück oder null, wenn belegt/nicht gefunden. */
  suspend fun bookSlot(slotId: Int, userId: Int): AppointmentRecord?

  suspend fun listForUser(userId: Int): List<AppointmentRecord>

  /** Storniert (setzt user==null), nur wenn der Termin dem User gehört. */
  suspend fun cancel(appointmentId: Int, userId: Int): Boolean
}

object DefaultAppointmentRepository : AppointmentRepository {

  override suspend fun listFreeSlots(
    officeId: Int,
    from: Instant?,
    to: Instant?
  ): List<AppointmentRecord> = newSuspendedTransaction(Dispatchers.IO) {
    val base: Query = Appointments
      .selectAll()
      .apply { andWhere { (Appointments.office eq officeId) and Appointments.user.isNull() } }

    if (from != null) base.andWhere { Appointments.startsAt greaterEq from }
    if (to != null) base.andWhere { Appointments.endsAt lessEq to }

    base.orderBy(Appointments.startsAt to SortOrder.ASC)
      .map { it.toRecord() }
  }

  override suspend fun createSlots(
    officeId: Int,
    slots: List<Pair<Instant, Instant>>
  ): List<AppointmentRecord> = newSuspendedTransaction(Dispatchers.IO) {
    val office = OfficeEntity.findById(officeId) ?: error("Office not found")
    slots.map { (s, e) ->
      AppointmentEntity.new {
        this.office = office
        this.startsAt = s
        this.endsAt = e
        this.user = null
      }.toRecord()
    }
  }

  override suspend fun deleteSlot(officeId: Int, slotId: Int): Boolean =
    newSuspendedTransaction(Dispatchers.IO) {
      val entity = AppointmentEntity.findById(slotId) ?: return@newSuspendedTransaction false
      if (entity.office.id.value != officeId) return@newSuspendedTransaction false
      if (entity.user != null) return@newSuspendedTransaction false // nur freie Slots
      entity.delete()
      true
    }

  override suspend fun findById(id: Int): AppointmentRecord? =
    newSuspendedTransaction(Dispatchers.IO) {
      AppointmentEntity.findById(id)?.toRecord()
    }

  override suspend fun bookSlot(slotId: Int, userId: Int): AppointmentRecord? =
    newSuspendedTransaction(Dispatchers.IO) {
      val entity = AppointmentEntity.findById(slotId) ?: return@newSuspendedTransaction null
      if (entity.user != null) return@newSuspendedTransaction null // bereits belegt
      val user = UserEntity.findById(userId) ?: return@newSuspendedTransaction null
      entity.user = user
      entity.toRecord()
    }

  override suspend fun listForUser(userId: Int): List<AppointmentRecord> =
    newSuspendedTransaction(Dispatchers.IO) {
      AppointmentEntity.find { Appointments.user eq EntityID(userId, Users) }
        .orderBy(Appointments.startsAt to SortOrder.ASC)
        .map { it.toRecord() }
    }

  override suspend fun cancel(appointmentId: Int, userId: Int): Boolean =
    newSuspendedTransaction(Dispatchers.IO) {
      val entity = AppointmentEntity.findById(appointmentId) ?: return@newSuspendedTransaction false
      if (entity.user?.id?.value != userId) return@newSuspendedTransaction false
      entity.user = null
      true
    }

  // --- mapping helper ---

  private fun ResultRow.toRecord() = AppointmentRecord(
    id = this[Appointments.id].value,
    officeId = this[Appointments.office].value,
    userId = this[Appointments.user]?.value,
    startsAt = this[Appointments.startsAt],
    endsAt = this[Appointments.endsAt]
  )

  private fun AppointmentEntity.toRecord() = AppointmentRecord(
    id = id.value,
    officeId = office.id.value,
    userId = user?.id?.value,
    startsAt = startsAt,
    endsAt = endsAt
  )
}
